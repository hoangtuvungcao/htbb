package io;

/**
 * CHIA GOI LON THANH MANH (0.9.7.1, su co "ket tai du lieu" cua client J2ME tren gia lap iOS).
 *
 * Client J2ME khong co zlib (khong nhan goi nen io.GoiNen) nen nhan nguyen khung goi lon luc dang nhap: lenh 26
 * ~159 KB, lenh 25 ~111 KB. Client cu cap phat MOT mang byte[] lien khoi bang do dai goi roi moi xu ly; tren may /
 * gia lap heap nho (bo gom rac khong don ngan) mang lien khoi 159 KB co the khong cap duoc du tong bo nho trong con
 * du -> OutOfMemoryError -> luong mang chet im lang (client 0.9.7 chi bat Exception) -> man tai dung mai, ket noi
 * van song nho goi giu ket noi 30 s nen server khong thay gi.
 *
 * Client tu bao bit KHA_NANG_MANH (so nguyen kha nang sau so ban o goi dang nhap) thi server chia moi khung goi
 * >= NGUONG byte thanh cac goi lenh LENH_MANH (-126) lien tiep, moi goi <= CO_MANH byte du lieu:
 *   manh dau : [0][lenh goc][do dai du lieu goc: int][phan dau du lieu]
 *   manh sau : [1][phan tiep theo cua du lieu]
 * Client gom cac manh (khong ghep thanh mot mang lon) roi xu ly goi goc qua mot luong doc chay qua cac manh. Cac
 * manh cua mot goi duoc xep LIEN TIEP trong hang doi gui (Session.Sender khoa khi xep), khong goi nao chen giua.
 * Moi manh van qua ma hoa XOR nhu moi goi khac. Client khong bao bit -> khong doi gi (client 0.9.7 tro ve truoc).
 */
public final class GoiManh {
    private GoiManh() {
    }

    /** Lenh goi manh (-127 la goi nen, -128 la tien to do dai 4 byte). */
    public static final int LENH_MANH = -126;
    /** Bit trong Session.khaNang: client gom duoc goi LENH_MANH. */
    public static final int KHA_NANG_MANH = 2;
    /** sv.manhGoi: 0 = tat han. */
    public static volatile boolean BAT = true;
    /** sv.manhGoiTu: chi chia khung du lieu >= so byte nay. */
    public static volatile int NGUONG = 16384;
    /** So byte du lieu toi da moi manh (khung manh luon < 32768 -> do dai 2 byte). */
    public static volatile int CO_MANH = 8192;

    public static final java.util.concurrent.atomic.AtomicLong SO_GOI_CHIA = new java.util.concurrent.atomic.AtomicLong();

    /**
     * Chia khung goi goc (Message.toByteArray, chua ma hoa) thanh cac khung LENH_MANH.
     * @return cac khung theo thu tu, hoac null neu khong chia (tat, nho, khung -40 / goi nen / hong).
     */
    public static byte[][] chia(final byte[] khung) {
        if (!BAT || khung == null || khung.length < 3) {
            return null;
        }
        int lenh, dai, dau;
        if (khung[0] == -128) {
            if (khung.length < 6) {
                return null;
            }
            lenh = khung[1];
            dai = ((khung[2] & 255) << 24) | ((khung[3] & 255) << 16) | ((khung[4] & 255) << 8) | (khung[5] & 255);
            dau = 6;
        } else {
            lenh = khung[0];
            dai = ((khung[1] & 255) << 8) | (khung[2] & 255);
            dau = 3;
        }
        if (lenh == -40 || lenh == LENH_MANH || lenh == GoiNen.LENH_NEN || dai < NGUONG || dau + dai != khung.length) {
            return null;
        }
        final int co = Math.max(256, Math.min(CO_MANH, 32000));
        final int dauManh = 6;   // [0][lenh][int do dai]
        final int soManh = 1 + Math.max(0, (dai - (co - dauManh) + (co - 1) - 1) / (co - 1));
        final byte[][] ra = new byte[soManh][];
        int off = dau;
        final int het = dau + dai;
        for (int k = 0; k < soManh; k++) {
            final int phan = k == 0 ? Math.min(co - dauManh, het - off) : Math.min(co - 1, het - off);
            final int n = (k == 0 ? dauManh : 1) + phan;
            final byte[] m = new byte[3 + n];
            m[0] = (byte) LENH_MANH;
            m[1] = (byte) (n >>> 8);
            m[2] = (byte) n;
            int i = 3;
            if (k == 0) {
                m[i++] = 0;
                m[i++] = (byte) lenh;
                m[i++] = (byte) (dai >>> 24);
                m[i++] = (byte) (dai >>> 16);
                m[i++] = (byte) (dai >>> 8);
                m[i++] = (byte) dai;
            } else {
                m[i++] = 1;
            }
            System.arraycopy(khung, off, m, i, phan);
            off += phan;
            ra[k] = m;
        }
        if (off != het) {
            return null; // khong bao gio xay ra - chot an toan: gui nguyen khung
        }
        SO_GOI_CHIA.incrementAndGet();
        return ra;
    }
}
