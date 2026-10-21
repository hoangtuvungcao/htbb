package io;

import java.util.zip.Deflater;

/**
 * NEN GOI LON (19/10/2026, open test "tai du lieu cham, khong vao duoc").
 *
 * Moi lan dang nhap server gui ~330 KB truoc khi nguoi choi vao duoc ban do, bat ke client da co
 * cache hay chua: lenh 26 (mau quai + du lieu hieu ung, ~159 KB), lenh 25 (mau vat pham, ~111 KB),
 * hai lan lenh 1 (~17 KB, lan dau kem tile ~24 KB neu vsTile=-1), lenh 105 (~19 KB). Do bang
 * KiemTaiThat (client PC that, cache rong): qua link 40 KB/s + 150 ms tre mat ~9.4 s tu dang nhap
 * toi vao the gioi; mang di dong yeu hon nua la 15-25 s - dung khoang "goi cuoi 8-24 s truoc" roi
 * nguoi choi tat game. Deflate muc 6 con ~116 KB (lenh 25: 111 -> 24 KB, 26: 159 -> 67 KB).
 *
 * Chi nen cho client TU BAO co giai nen (bit KHA_NANG_NEN trong so nguyen kha nang gui sau so ban
 * o goi dang nhap - client 0.9.6.x tro ve truoc khong gui nen khong bao gio nhan goi nen).
 *
 * Dinh dang: mot goi lenh LENH_NEN (-127) binh thuong (van qua ma hoa XOR nhu moi goi), du lieu =
 * deflate (zlib) cua MOT khung goi goc chua ma hoa, dung dinh dang Message.toByteArray():
 * [-128] lenh [4 byte | 2 byte] do dai, du lieu. Client giai nen roi xu ly khung ben trong nhu vua
 * doc tu socket. Thu tu goi giu nguyen (cung hang doi gui).
 */
public final class GoiNen {
    private GoiNen() {
    }

    /** Lenh boc goi nen (khong lenh nao khac dung -127: -128 la tien to do dai 4 byte). */
    public static final int LENH_NEN = -127;
    /** Bit trong Session.khaNang: client giai nen duoc goi LENH_NEN. */
    public static final int KHA_NANG_NEN = 1;

    /** sv.nenGoi: 0 = tat han (mac dinh bat - chi co tac dung voi client co bit KHA_NANG_NEN). */
    public static volatile boolean BAT = true;
    /** sv.nenGoiTu: chi nen khung >= so byte nay (goi nho nen khong dang - ton CPU, loi it). */
    public static volatile int NGUONG = 2048;

    public static final java.util.concurrent.atomic.AtomicLong BYTE_GOC = new java.util.concurrent.atomic.AtomicLong();
    public static final java.util.concurrent.atomic.AtomicLong BYTE_NEN = new java.util.concurrent.atomic.AtomicLong();

    private static final ThreadLocal<Deflater> BO_NEN = new ThreadLocal<Deflater>() {
        protected Deflater initialValue() {
            return new Deflater(6);
        }
    };

    /**
     * Boc khung goi goc (Message.toByteArray, chua ma hoa) thanh khung goi LENH_NEN.
     * @return khung moi, hoac null neu khong nen (tat, nho, hoac nen khong loi duoc it nhat 1/8).
     */
    public static byte[] boc(final byte[] khung) {
        if (!BAT || khung == null || khung.length < NGUONG || khung.length < 8) {
            return null;
        }
        // khung -40 (gui khoa) khong bao gio qua day (truoc khi co kha nang), nhung chan cho chac
        if (khung[0] == -40) {
            return null;
        }
        final Deflater d = BO_NEN.get();
        d.reset();
        d.setInput(khung);
        d.finish();
        byte[] ra = new byte[khung.length + 64];
        int n = 0;
        while (!d.finished()) {
            if (n == ra.length) {
                ra = java.util.Arrays.copyOf(ra, ra.length * 2);
            }
            n += d.deflate(ra, n, ra.length - n);
        }
        if (n > khung.length - (khung.length >> 3)) {
            return null; // khong dang: du lieu da nen san (anh PNG ...)
        }
        final boolean lon = n > 32767;
        final int dau = lon ? 6 : 3;
        final byte[] kq = new byte[dau + n];
        int i = 0;
        if (lon) {
            kq[i++] = (byte) -128;
        }
        kq[i++] = (byte) LENH_NEN;
        if (lon) {
            kq[i++] = (byte) (n >>> 24);
            kq[i++] = (byte) (n >>> 16);
        }
        kq[i++] = (byte) (n >>> 8);
        kq[i++] = (byte) n;
        System.arraycopy(ra, 0, kq, i, n);
        BYTE_GOC.addAndGet(khung.length);
        BYTE_NEN.addAndGet(kq.length);
        return kq;
    }
}
