package com.htbb.pokemon;

import java.util.Random;

/**
 * CA THE cua mot Pokemon trong Hop PC (docs/thiet-ke-game.md 7.7): hai con cung loai KHAC nhau nhu game chinh ban.
 * <ul>
 * <li><b>Chi so ca the</b> (IV) 0..31 cho 6 chi so {HP, Tan cong, Phong thu, Tan cong dac biet, Phong thu dac biet,
 *     Toc do}, dong goi 6 x 5 bit trong mot so nguyen (0..2^30-1);</li>
 * <li><b>Tinh cach</b> 0..24 theo thu tu game chinh ban (Hardy .. Quirky): +10% mot chi so, -10% mot chi so (5 tinh
 *     cach trung tinh);</li>
 * <li><b>Danh gia</b> 0..3 sao theo % tong IV (kieu "tham dinh"): &gt;= 82% 3 sao, &gt;= 67% 2 sao, &gt;= 51% 1 sao;
 *     100% = "Hoàn hảo";</li>
 * <li><b>Chi so</b> theo cong thuc game chinh ban (khong EV): HP = (2B + IV) x L / 100 + L + 10; khac =
 *     ((2B + IV) x L / 100 + 5) x tinh cach; <b>Sức mạnh</b> = tong 6 chi so.</li>
 * </ul>
 * CHI SO NAY CHI THUOC CON TRONG HOP PC: khong cong vao chi so nguoi choi (ban dong hanh 0 chi so, spec 4.6 de "dong
 * dieu" cho ban 1.1). Dung cho: danh gia khi giu / chuyen doi (giu con tot hon), phi trao doi (con 3 sao phi x2), hien
 * thi. Lop THUAN (test/KiemTraoDoiPokemon).
 */
public final class CaThePokemon {

    public static final int IV_TOI_DA = 31;
    public static final int SO_CHI_SO = 6;
    public static final int SO_TINH_CACH = 25;
    public static final String[] TEN_CHI_SO = {"HP", "Tấn công", "Phòng thủ", "Tấn công đặc biệt", "Phòng thủ đặc biệt", "Tốc độ"};
    /** Ten tinh cach theo chi so game chinh ban (Hardy, Lonely, Brave, Adamant, Naughty, Bold, ... Quirky). */
    public static final String[] TEN_TINH_CACH = {
        "Cứng cỏi", "Cô đơn", "Dũng cảm", "Kiên quyết", "Nghịch ngợm",
        "Táo bạo", "Ngoan ngoãn", "Thư thái", "Tinh quái", "Lơ đễnh",
        "Nhút nhát", "Hấp tấp", "Nghiêm túc", "Vui vẻ", "Ngây thơ",
        "Khiêm tốn", "Ôn hoà", "Trầm lặng", "Bẽn lẽn", "Liều lĩnh",
        "Điềm tĩnh", "Hiền lành", "Láo lỉnh", "Cẩn thận", "Kỳ quặc"};
    /** Thu tu chi so cua bang tinh cach (Tan cong, Phong thu, Toc do, Tan cong DB, Phong thu DB) -&gt; chi so 6 o. */
    static final int[] O_TINH_CACH = {1, 2, 5, 3, 4};
    /** Chi so goc mac dinh khi loai chua co du lieu (trung binh mot loai chua tien hoa). */
    public static final int[] GOC_MAC_DINH = {60, 60, 60, 60, 60, 60};

    private CaThePokemon() {
    }

    // ------------------------------------------------------------------ IV

    public static int iv(final int goi, final int i) {
        return (goi >>> (5 * i)) & 31;
    }

    public static int dongGoi(final int[] iv) {
        int g = 0;
        for (int i = 0; i < SO_CHI_SO; i++) {
            g |= (Math.max(0, Math.min(IV_TOI_DA, iv[i])) & 31) << (5 * i);
        }
        return g;
    }

    public static int[] moGoi(final int goi) {
        final int[] v = new int[SO_CHI_SO];
        for (int i = 0; i < SO_CHI_SO; i++) {
            v[i] = iv(goi, i);
        }
        return v;
    }

    /** IV ngau nhien (moi chi so 0..31 deu nhau, nhu game chinh ban). */
    public static int ivNgauNhien(final Random r) {
        final int[] v = new int[SO_CHI_SO];
        for (int i = 0; i < SO_CHI_SO; i++) {
            v[i] = r.nextInt(IV_TOI_DA + 1);
        }
        return dongGoi(v);
    }

    public static int tinhCachNgauNhien(final Random r) {
        return r.nextInt(SO_TINH_CACH);
    }

    /** IV tat dinh cho con cu (luu truoc ban co IV): bam id + dex + ngay, on dinh qua moi lan nap. */
    public static int ivTatDinh(final int id, final int dex, final int ngay) {
        long h = 1469598103934665603L;
        final int[] k = {id, dex, ngay, 0x5eed};
        for (final int x : k) {
            h ^= x;
            h *= 1099511628211L;
        }
        return (int) ((h ^ (h >>> 31)) & 0x3FFFFFFF);
    }

    public static int tinhCachTatDinh(final int id, final int dex, final int ngay) {
        return Math.abs(ivTatDinh(id + 7, dex, ngay) % SO_TINH_CACH);
    }

    public static int tong(final int goi) {
        int t = 0;
        for (int i = 0; i < SO_CHI_SO; i++) {
            t += iv(goi, i);
        }
        return t;
    }

    /** % tong IV (0..100, lam tron xuong; 186/186 = 100). */
    public static int phanTram(final int goi) {
        return tong(goi) * 100 / (IV_TOI_DA * SO_CHI_SO);
    }

    /** Danh gia 0..3 sao. */
    public static int sao(final int goi) {
        final int p = phanTram(goi);
        if (p >= 82) {
            return 3;
        }
        if (p >= 67) {
            return 2;
        }
        if (p >= 51) {
            return 1;
        }
        return 0;
    }

    public static boolean hoanHao(final int goi) {
        return tong(goi) == IV_TOI_DA * SO_CHI_SO;
    }

    public static String chuDanhGia(final int goi) {
        if (hoanHao(goi)) {
            return "Hoàn hảo";
        }
        switch (sao(goi)) {
            case 3:
                return "Xuất sắc";
            case 2:
                return "Tốt";
            case 1:
                return "Khá";
            default:
                return "Bình thường";
        }
    }

    // ------------------------------------------------------------------ tinh cach

    public static String tenTinhCach(final int tc) {
        return TEN_TINH_CACH[Math.max(0, Math.min(SO_TINH_CACH - 1, tc))];
    }

    /** O chi so tang (1..5) hoac -1 neu trung tinh. */
    public static int oTang(final int tc) {
        final int a = Math.max(0, Math.min(SO_TINH_CACH - 1, tc)) / 5;
        final int b = Math.max(0, Math.min(SO_TINH_CACH - 1, tc)) % 5;
        return a == b ? -1 : O_TINH_CACH[a];
    }

    public static int oGiam(final int tc) {
        final int a = Math.max(0, Math.min(SO_TINH_CACH - 1, tc)) / 5;
        final int b = Math.max(0, Math.min(SO_TINH_CACH - 1, tc)) % 5;
        return a == b ? -1 : O_TINH_CACH[b];
    }

    /** "+Tấn công −Tốc độ" / "trung tính". */
    public static String chuTinhCach(final int tc) {
        final int t = oTang(tc);
        if (t < 0) {
            return tenTinhCach(tc) + " (trung tính)";
        }
        return tenTinhCach(tc) + " (+" + TEN_CHI_SO[t] + ", -" + TEN_CHI_SO[oGiam(tc)] + ")";
    }

    // ------------------------------------------------------------------ chi so / suc manh

    public static int[] chiSo(final int[] goc, final int goi, final int tc, final int cap) {
        final int[] b = goc != null && goc.length == SO_CHI_SO ? goc : GOC_MAC_DINH;
        final int l = Math.max(1, Math.min(100, cap));
        final int[] v = new int[SO_CHI_SO];
        final int tang = oTang(tc);
        final int giam = oGiam(tc);
        for (int i = 0; i < SO_CHI_SO; i++) {
            final int bi = Math.max(1, Math.min(255, b[i]));
            final int x = (2 * bi + iv(goi, i)) * l / 100;
            if (i == 0) {
                v[i] = x + l + 10;
            } else {
                int s = x + 5;
                if (i == tang) {
                    s = s * 110 / 100;
                } else if (i == giam) {
                    s = s * 90 / 100;
                }
                v[i] = s;
            }
        }
        return v;
    }

    /** Sức mạnh = tong 6 chi so. */
    public static int sucManh(final int[] goc, final int goi, final int tc, final int cap) {
        int t = 0;
        for (final int x : chiSo(goc, goi, tc, cap)) {
            t += x;
        }
        return t;
    }
}
