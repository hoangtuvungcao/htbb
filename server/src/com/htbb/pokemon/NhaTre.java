package com.htbb.pokemon;

import java.util.Random;

/**
 * Nha tre Pokemon + trung (docs/thiet-ke-game.md). Lop THUAN.
 *
 *   - Gui 1-2 pet DANG SO HUU (Char.pet, nhan dang qua PetPokemon.dex) vao nha tre, tra phi
 *     {@link HeSinhThai#PHI_NHA_TRE_NGAY_PHAN_NGHIN}‰ gio moi ngay (ho hut xu).
 *   - Cu {@link HeSinhThai#BUOC_DE_TRUNG} "buoc" (moi quai chu nhan ha = 1 buoc - tinh theo thoi gian
 *     choi THAT, treo may khong nhanh hon vi da bi nang luong huan luyen chan) nha tre de 1 trung,
 *     toi da {@link HeSinhThai#TRUNG_TOI_DA_NGAY}/ngay.
 *   - Trung no sau {@link #BUOC_NO} buoc theo loai. IV (HP/cong/thu 0..31) di truyen tu cha me
 *     (moi chi so lay cua 1 trong 2), {@link HeSinhThai#IV_DOT_BIEN_PHAN_TRAM}% dot bien +-1..3.
 *   - Shiny 1/{@link HeSinhThai#SHINY_TRUNG_MAU}, pity: trung thu {@link HeSinhThai#SHINY_TRUNG_PITY}
 *     ke tu lan shiny truoc chac chan shiny.
 *   - Trung la HANG HOA: ban tren cho sinh thai (thue 6% + phi dang 1%), NPC khong mua.
 *   - IV cao (pham chat S: tong >= 84/93) no ra pet cap 2 neu loai co cap 2 (tiet kiem nang cap pet).
 */
public final class NhaTre {

    private NhaTre() {
    }

    /** {dex loai goc (trung), id item pet cap 1, id item pet cap 2 (-1 = khong co), buoc no}. */
    public static final int[][] LOAI_TRUNG = {
        {163, 689, 690, 400},   // Hoothoot
        {147, 694, 695, 800},   // Dratini (hiem)
        {173, 699, 700, 500},   // Cleffa -> Clefairy
        {41, 704, 705, 300},    // Zubat
        {627, 709, 710, 600},   // Rufflet
        {77, 681, -1, 400},     // Ponyta
        {417, 727, -1, 300},    // Pachirisu
        {225, 728, -1, 400},    // Delibird
        {667, 803, -1, 400},    // Litleo
        {58, 804, -1, 400},     // Growlithe
        {585, 832, -1, 300},    // Deerling
        {399, 873, -1, 250},    // Bidoof
    };

    /** Dex cua pet (moi dang tien hoa) -> dex loai goc de ra trung. 0 = khong ra trung duoc. */
    public static int loaiGoc(final int dex) {
        switch (dex) {
            case 164:
                return 163;
            case 148:
            case 149:
                return 147;
            case 35:
            case 36:
                return 173;
            case 42:
            case 169:
                return 41;
            case 628:
                return 627;
            case 78:
                return 77;
            default:
                break;
        }
        return chiSo(dex) >= 0 ? dex : 0;
    }

    /** Chi so trong LOAI_TRUNG cua dex goc, -1 neu khong co. */
    public static int chiSo(final int dexGoc) {
        for (int i = 0; i < LOAI_TRUNG.length; i++) {
            if (LOAI_TRUNG[i][0] == dexGoc) {
                return i;
            }
        }
        return -1;
    }

    public static int buocNo(final int dexGoc) {
        final int i = chiSo(dexGoc);
        return i < 0 ? 400 : LOAI_TRUNG[i][3];
    }

    /** Mot qua trung. Chuoi hoa: "dex.hp.cong.thu.shiny.buocDaDi" (luu trong ho so / tin ban). */
    public static final class Trung {
        public int dex, ivHp, ivCong, ivThu, buoc;
        public boolean shiny;

        public Trung(final int dex, final int ivHp, final int ivCong, final int ivThu, final boolean shiny, final int buoc) {
            this.dex = dex;
            this.ivHp = ivHp;
            this.ivCong = ivCong;
            this.ivThu = ivThu;
            this.shiny = shiny;
            this.buoc = buoc;
        }

        public int tongIv() {
            return ivHp + ivCong + ivThu;
        }

        public String chuoi() {
            return dex + "." + ivHp + "." + ivCong + "." + ivThu + "." + (shiny ? 1 : 0) + "." + buoc;
        }

        public static Trung doc(final String s) {
            if (s == null) {
                return null;
            }
            final String[] a = s.trim().split("\\.");
            if (a.length < 6) {
                return null;
            }
            try {
                final Trung t = new Trung(Integer.parseInt(a[0]), kep(Integer.parseInt(a[1])), kep(Integer.parseInt(a[2])),
                        kep(Integer.parseInt(a[3])), "1".equals(a[4]), Math.max(0, Integer.parseInt(a[5])));
                return chiSo(t.dex) >= 0 ? t : null;
            } catch (final NumberFormatException e) {
                return null;
            }
        }

        public boolean daNo() {
            return buoc >= buocNo(dex);
        }

        public String moTa() {
            return SuuTamSinhThai.tenLoai(dex) + (shiny ? " (shiny)" : "") + " IV " + ivHp + "/" + ivCong + "/" + ivThu
                    + " (" + phamChat(tongIv()) + ")" + (daNo() ? " - sắp nở" : " - " + buoc + "/" + buocNo(dex) + " bước");
        }
    }

    static int kep(final int iv) {
        return Math.max(0, Math.min(HeSinhThai.IV_TOI_DA, iv));
    }

    /** Pham chat theo tong IV (0..93): S >= 84, A >= 70, B >= 50, C con lai. */
    public static String phamChat(final int tong) {
        if (tong >= 84) {
            return "S";
        }
        if (tong >= 70) {
            return "A";
        }
        if (tong >= 50) {
            return "B";
        }
        return "C";
    }

    /** IV "hoang da" cua mot pet khong no tu trung: tat dinh theo (nhan vat, dex), 0..15 moi chi so. */
    public static int[] ivHoangDa(final int idNhanVat, final int dex) {
        final long h = SinhThaiTheGioi.bam(idNhanVat * 1000003L + dex);
        return new int[]{(int) (h & 15), (int) ((h >>> 8) & 15), (int) ((h >>> 16) & 15)};
    }

    /** IV con: moi chi so lay ngau nhien cua cha hoac me (me = cha neu 1 pet), dot bien +-1..3. */
    public static int[] diTruyen(final int[] cha, final int[] me, final Random r) {
        final int[] b = me == null ? cha : me;
        final int[] con = new int[3];
        for (int i = 0; i < 3; i++) {
            int v = r.nextBoolean() ? cha[i] : b[i];
            if (r.nextInt(100) < HeSinhThai.IV_DOT_BIEN_PHAN_TRAM) {
                v += (r.nextBoolean() ? 1 : -1) * (1 + r.nextInt(3));
            }
            con[i] = kep(v);
        }
        return con;
    }

    /** Shiny? {@code demKhongShiny} = so trung lien tiep chua shiny TRUOC qua nay. */
    public static boolean laShiny(final int demKhongShiny, final Random r) {
        if (demKhongShiny + 1 >= HeSinhThai.SHINY_TRUNG_PITY) {
            return true;
        }
        return r.nextInt(HeSinhThai.SHINY_TRUNG_MAU) == 0;
    }

    /** Chuoi thuong khi no: pet cap 2 neu pham chat S va co cap 2, nguoc lai cap 1. */
    public static String thuongNo(final Trung t) {
        final int i = chiSo(t.dex);
        if (i < 0) {
            return null;
        }
        final int id = t.tongIv() >= 84 && LOAI_TRUNG[i][2] > 0 ? LOAI_TRUNG[i][2] : LOAI_TRUNG[i][1];
        return "ITEM:" + id + ":1";
    }

    /** So trung de them sau khi cong {@code buoc} buoc (buocTichLuy: so du truoc), co tran ngay. */
    public static int soTrungMoi(final int buocTichLuy, final int buocThem, final int daDeHomNay) {
        final int tong = Math.max(0, buocTichLuy) + Math.max(0, buocThem);
        final int de = tong / HeSinhThai.BUOC_DE_TRUNG;
        return Math.max(0, Math.min(de, HeSinhThai.TRUNG_TOI_DA_NGAY - daDeHomNay));
    }
}
