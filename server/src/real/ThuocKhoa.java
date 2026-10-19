package real;

import com.htbb.pokemon.HoSoLauDai;

/**
 * Thuoc KHOA (chong clone 19/10/2026). Thuoc la thu DUY NHAT giao dich truc tiep duoc (real/MapMuaBan), nhung
 * mang potions[] khong co co khoa: binh mua bang LUONG KHOA (shop luong khi sv.lau_dai tra bang luong khoa, mon
 * 127/128 o shop luong khoa) truoc day giao dich duoc -> luong khoa doi ra gia tri chuyen duoc cho nguoi khac.
 *
 * Cach lam: dem rieng so binh khoa theo id trong ho so dai han (HoSoLauDai.thuocKhoa, khoa "x<id>"). So giao dich
 * duoc = potions[id] - min(khoa, potions[id]). Dung thuoc thi tru phan KHONG khoa truoc (dem khoa chi bi kep
 * xuong bang so dang co) - nguoi choi chiu thiet mot chut, khong bao gio lot. Tat sv.lau_dai (khong co ho so)
 * thi khong dem - khi do shop luong cung khong tra bang luong khoa.
 */
public final class ThuocKhoa {

    private ThuocKhoa() {
    }

    /** So binh {@code id} dang khoa (da kep <= so dang co). */
    public static int soKhoa(final Char p, final int id) {
        if (p == null || p.potions == null || id < 0 || id >= p.potions.length) {
            return 0;
        }
        final HoSoLauDai h = LauDaiNhanVat.lay(p);
        if (h == null) {
            return 0;
        }
        final int k = h.thuocKhoa(id);
        if (k <= 0) {
            return 0;
        }
        final int co = Math.max(0, p.potions[id]);
        if (k > co) {
            h.datThuocKhoa(id, co);
            return co;
        }
        return k;
    }

    /** So binh {@code id} giao dich duoc. */
    public static int giaoDichDuoc(final Char p, final int id) {
        if (p == null || p.potions == null || id < 0 || id >= p.potions.length) {
            return 0;
        }
        return giaoDichDuoc(p.potions[id], soKhoa(p, id));
    }

    /** Ham thuan: co {@code coSan} binh, {@code khoa} binh khoa -> so giao dich duoc. */
    public static int giaoDichDuoc(final int coSan, final int khoa) {
        final int co = Math.max(0, coSan);
        return co - Math.min(Math.max(0, khoa), co);
    }

    /** Them {@code n} binh khoa (vua mua bang luong khoa). */
    public static void themKhoa(final Char p, final int id, final int n) {
        if (p == null || n <= 0 || p.potions == null || id < 0 || id >= p.potions.length) {
            return;
        }
        final HoSoLauDai h = LauDaiNhanVat.lay(p);
        if (h == null) {
            return;
        }
        h.datThuocKhoa(id, Math.min(Math.max(0, p.potions[id]), h.thuocKhoa(id) + n));
    }

    /**
     * Sau mot lan mua o shop (lenh 85): neu luong khoa da giam thi moi binh thuoc moi tang deu la thuoc khoa.
     * Thuan de test: {@code truoc} la ban sao potions truoc khi mua.
     */
    public static void sauKhiMua(final Char p, final int[] truoc, final long luongKhoaTruoc) {
        if (p == null || truoc == null || p.potions == null) {
            return;
        }
        if (p.getLuongLock() >= luongKhoaTruoc) {
            return; // khong tieu luong khoa
        }
        final int n = Math.min(truoc.length, p.potions.length);
        for (int i = 0; i < n; i++) {
            final int tang = p.potions[i] - truoc[i];
            if (tang > 0) {
                themKhoa(p, i, tang);
            }
        }
    }

    /**
     * Kiem lai cuoi giao dich: moi binh {@code ben} dua vao ({@code dua[id]}) phai nam trong phan giao dich duoc.
     * Tra id thuoc vi pham dau tien, -1 neu hop le.
     */
    public static int viPham(final Char ben, final int[] dua) {
        if (ben == null || dua == null) {
            return -1;
        }
        for (int i = 0; i < dua.length; i++) {
            if (dua[i] > 0 && dua[i] > giaoDichDuoc(ben, i)) {
                return i;
            }
        }
        return -1;
    }
}
