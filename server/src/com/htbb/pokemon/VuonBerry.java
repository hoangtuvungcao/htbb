package com.htbb.pokemon;

/**
 * VUON BERRY (bat lai 29/10/2026, chu game "Bật lại vườn"). Lop THUAN: so lieu + luat gieo / lon / thu hoach tren
 * {@link HoSoSinhThai} (menu o HeSinhThaiMenu: Hệ sinh thái → Vườn nhà và bạn bè → Ô đất của tôi).
 *
 * Truoc day "Trồng và thu hoạch …" (N-x.12, TV-05.2, TV-08.1, AN-14, HT-CEL.3, HT-SUI.4, C54.4 …) khong bao gio xong: o dat
 * cu (Char.seedsItem / tob_farm, ban do vuon 29 cua game goc - nay la Rung Ilex) chi do Database.getFarm nap, ma khong
 * ai goi. Vuon moi KHONG dung lai duong cu do (mua o dat mien phi, hat mien phi, goi -59 ve o dat len Rung Ilex): o dat
 * nam trong ho so he sinh thai (tob_hst_nhan_vat, cung dong voi Tui sinh thai) - gieo / thu hoach doi o dat va tui trong
 * MOT lan ghi, nen dang nhap lai / rot mang / hai phien khong nhan doi duoc.
 *
 * Luat (moi so o day, test/real/KiemVuonBerry kiem):
 *  - O dat: {@link #O_CO_BAN} o mien phi, +1 o moi moc cap {@link #CAP_THEM_O}, mua them toi da {@link #O_MUA_TOI_DA} o
 *    bang xu (ho hut: {@link #GIA_MO_O_PHAN_NGHIN} ‰ gio thu nhap o cap nguoi choi). Toi da {@link #O_TOI_DA} o.
 *  - Hat giong: CHI mua o vuon luc gieo, gia {@link #GIA_HAT_LAN} x gia tham chieu 1 berry (bac theo cap). Berry trong tui
 *    KHONG gieo duoc (khong tu nhan ban mien phi).
 *  - Lon theo dong ho may chu ({@link DongHo#bay()}): {@link #PHUT} phut. Chin roi giu nguyen (khong heo), thu luc nao cung duoc.
 *  - Thu hoach: {@link #SAN_LUONG} berry (bac luc gieo) vao Tui sinh thai, +{@link #THEM_KHI_TUOI} neu hom nay vuon duoc
 *    ban be tuoi tu {@link #TUOI_DE_THEM} lan. Toi da {@link #THU_TOI_DA_NGAY} lan thu / ngay.
 *  - Khong lam phat: NPC mua lai 40% gia tham chieu -> san luong toi da (4) ban NPC = 1,6 x gia tham chieu < gia hat
 *    (2 x): gieo roi ban NPC luon LO xu (vuon la ho hut, khong phai voi xu). Tran ban NPC 0,1 gio / ngay van giu.
 */
public final class VuonBerry {

    private VuonBerry() {
    }

    /** Ma vat lieu berry gieo duoc (VatLieuSinhThai 30..37) va cap nhan vat toi thieu, phut lon. Them cuoi, khong doi thu tu. */
    public static final int[] MA = {30, 31, 32, 33, 34, 35, 36, 37};
    public static final int[] CAP = {1, 1, 5, 5, 10, 20, 30, 40};
    public static final int[] PHUT = {20, 20, 30, 30, 45, 60, 90, 120};

    public static final int SAN_LUONG = 3;
    public static final int THEM_KHI_TUOI = 1;
    public static final int TUOI_DE_THEM = 2;

    public static final int O_CO_BAN = 2;
    public static final int[] CAP_THEM_O = {15, 30};
    public static final int O_MUA_TOI_DA = 2;
    public static final int O_TOI_DA = O_CO_BAN + 2 + O_MUA_TOI_DA; // = 6 (CAP_THEM_O co 2 moc)
    /** Gia mo o mua them thu 1 / thu 2: ‰ gio thu nhap o cap nguoi choi (1 gio, 3 gio). */
    public static final int[] GIA_MO_O_PHAN_NGHIN = {1000, 3000};

    public static final int THU_TOI_DA_NGAY = 12;
    public static final int GIA_HAT_LAN = 2;

    /** Trang thai o. */
    public static final int KHOA = -1, TRONG = 0, DANG_LON = 1, CHIN = 2;

    /** Ket qua thu hoach (am = khong thu duoc). */
    public static final int CHUA_CHIN = -1, KHONG_CO_CAY = -2, HET_LUOT = -3;

    public static int chiSo(final int ma) {
        for (int i = 0; i < MA.length; i++) {
            if (MA[i] == ma) {
                return i;
            }
        }
        return -1;
    }

    public static String ten(final int ma) {
        return VatLieuSinhThai.ten(ma);
    }

    /** So o mien phi theo cap. */
    public static int soOMienPhi(final int cap) {
        int n = O_CO_BAN;
        for (final int c : CAP_THEM_O) {
            if (cap >= c) {
                n++;
            }
        }
        return n;
    }

    /** So o dung duoc (mien phi + da mua), tran O_TOI_DA. */
    public static int soO(final int cap, final int daMua) {
        return Math.min(O_TOI_DA, soOMienPhi(cap) + Math.max(0, Math.min(O_MUA_TOI_DA, daMua)));
    }

    /** Gia mo o mua them tiep theo; -1 = da mua het. */
    public static long giaMoO(final int cap, final int daMua) {
        if (daMua < 0 || daMua >= O_MUA_TOI_DA) {
            return -1L;
        }
        final int c = Math.max(1, Math.min(ThongSoLauDai.CAP_TOI_DA, cap));
        return Math.max(1L, ThongSoLauDai.xuMoiGio(c) * GIA_MO_O_PHAN_NGHIN[daMua] / 1000L);
    }

    /** Gia 1 hat (xu) o cap nguoi choi. */
    public static long giaHat(final int ma, final int cap) {
        return GIA_HAT_LAN * VatLieuSinhThai.giaXu(ma, VatLieuSinhThai.bacTheoCap(cap));
    }

    public static long msLon(final int ma) {
        final int i = chiSo(ma);
        return i < 0 ? 0L : PHUT[i] * 60000L;
    }

    /** San luong 1 lan thu theo so lan duoc tuoi hom nay. */
    public static int sanLuong(final int tuoiNhanHomNay) {
        return SAN_LUONG + (tuoiNhanHomNay >= TUOI_DE_THEM ? THEM_KHI_TUOI : 0);
    }

    public static int trangThai(final HoSoSinhThai h, final int o, final int cap, final long bay) {
        synchronized (h) {
            if (o < 0 || o >= O_TOI_DA) {
                return KHOA;
            }
            if (h.vuonMa[o] > 0) {
                return bay >= h.vuonGieo[o] + msLon(h.vuonMa[o]) ? CHIN : DANG_LON;
            }
            return o < soO(cap, h.vuonDaMua) ? TRONG : KHOA;
        }
    }

    /** Con bao nhieu phut (lam tron len) toi khi chin; 0 = chin / trong. */
    public static int conPhut(final HoSoSinhThai h, final int o, final long bay) {
        synchronized (h) {
            if (o < 0 || o >= O_TOI_DA || h.vuonMa[o] <= 0) {
                return 0;
            }
            final long con = h.vuonGieo[o] + msLon(h.vuonMa[o]) - bay;
            return con <= 0 ? 0 : (int) ((con + 59999L) / 60000L);
        }
    }

    /** Ly do khong gieo duoc berry {@code ma} (null = gieo duoc, chua tinh xu). */
    public static String lyDoKhongGieo(final HoSoSinhThai h, final int o, final int ma, final int cap, final long bay) {
        final int i = chiSo(ma);
        if (i < 0) {
            return "Không có hạt giống này.";
        }
        if (cap < CAP[i]) {
            return "Cần đạt cấp " + CAP[i] + " để trồng " + ten(ma) + ".";
        }
        final int tt = trangThai(h, o, cap, bay);
        if (tt == KHOA) {
            return "Ô đất này chưa mở.";
        }
        if (tt != TRONG) {
            return "Ô đất này đang có cây.";
        }
        return null;
    }

    /**
     * Dat cay vao o (sau khi DA tru tien hat). Nguyen tu: chi dat neu o con trong + mo. false = khong dat (goi phai hoan
     * tien hat).
     */
    public static boolean datGieo(final HoSoSinhThai h, final int o, final int ma, final int cap, final long bay) {
        synchronized (h) {
            if (lyDoKhongGieo(h, o, ma, cap, bay) != null) {
                return false;
            }
            h.vuonMa[o] = ma;
            h.vuonBac[o] = VatLieuSinhThai.bacTheoCap(cap);
            h.vuonGieo[o] = bay;
            return true;
        }
    }

    /**
     * Thu hoach o {@code o}: nguyen tu - xoa cay + cong berry vao tui + dem luot trong MOT khoi synchronized. Tra so
     * berry (&gt; 0) hoac {@link #CHUA_CHIN} / {@link #KHONG_CO_CAY} / {@link #HET_LUOT}. {@code maRa[0]} = ma berry da thu.
     */
    public static int thu(final HoSoSinhThai h, final int o, final long bay, final int[] maRa) {
        synchronized (h) {
            if (o < 0 || o >= O_TOI_DA || h.vuonMa[o] <= 0) {
                return KHONG_CO_CAY;
            }
            final int ma = h.vuonMa[o];
            if (bay < h.vuonGieo[o] + msLon(ma)) {
                return CHUA_CHIN;
            }
            if (h.thuVuonHomNay >= THU_TOI_DA_NGAY) {
                return HET_LUOT;
            }
            final int so = sanLuong(h.tuoiNhanHomNay);
            h.them(VatLieuSinhThai.khoa(ma, h.vuonBac[o]), so);
            h.vuonMa[o] = 0;
            h.vuonBac[o] = 0;
            h.vuonGieo[o] = 0L;
            h.thuVuonHomNay++;
            if (maRa != null && maRa.length > 0) {
                maRa[0] = ma;
            }
            return so;
        }
    }

    /** Ghi nhan mua them 1 o (sau khi DA tru tien). false = da mua het (goi hoan tien). */
    public static boolean datMuaO(final HoSoSinhThai h, final int daMuaKhiBao) {
        synchronized (h) {
            if (h.vuonDaMua != daMuaKhiBao || h.vuonDaMua >= O_MUA_TOI_DA) {
                return false;
            }
            h.vuonDaMua++;
            return true;
        }
    }

    /** So o dang chin. */
    public static int soChin(final HoSoSinhThai h, final long bay) {
        int n = 0;
        synchronized (h) {
            for (int o = 0; o < O_TOI_DA; o++) {
                if (h.vuonMa[o] > 0 && bay >= h.vuonGieo[o] + msLon(h.vuonMa[o])) {
                    n++;
                }
            }
        }
        return n;
    }

    /** Mot dong mo ta o (menu / tham vuon). */
    public static String moTaO(final HoSoSinhThai h, final int o, final int cap, final long bay) {
        final int tt = trangThai(h, o, cap, bay);
        final String dau = "Ô " + (o + 1) + ": ";
        switch (tt) {
            case CHIN:
                return dau + ten(h.vuonMa[o]) + " đã chín - thu hoạch";
            case DANG_LON:
                return dau + ten(h.vuonMa[o]) + " - còn " + conPhut(h, o, bay) + " phút";
            case TRONG:
                return dau + "trống - gieo hạt";
            default:
                return dau + "chưa mở";
        }
    }

    /** Chu huong dan (menu "Hướng dẫn trồng berry"). */
    public static String huongDan() {
        final StringBuilder b = new StringBuilder("Trồng berry: Hệ sinh thái → Vườn nhà và bạn bè → Ô đất của tôi → chọn ô trống → "
                + "chọn hạt (mua ngay lúc gieo). Cây lớn theo giờ máy chủ, kể cả khi bạn thoát game; chín rồi không héo, chọn ô"
                + " để thu hoạch " + SAN_LUONG + " berry vào Túi sinh thái (+" + THEM_KHI_TUOI + " nếu hôm nay vườn được bạn bè"
                + " tưới từ " + TUOI_DE_THEM + " lần). Tối đa " + THU_TOI_DA_NGAY + " lần thu hoạch mỗi ngày.\n");
        for (int i = 0; i < MA.length; i++) {
            b.append("- ").append(ten(MA[i])).append(": cấp ").append(CAP[i]).append(", ").append(PHUT[i]).append(" phút\n");
        }
        b.append("Ô đất: ").append(O_CO_BAN).append(" ô, thêm 1 ô ở cấp ").append(CAP_THEM_O[0]).append(" và cấp ")
                .append(CAP_THEM_O[1]).append(", mua thêm tối đa ").append(O_MUA_TOI_DA).append(" ô bằng xu (tối đa ")
                .append(O_TOI_DA).append(" ô). Nhiệm vụ \"Trồng và thu hoạch\" ở Sổ tay tính mỗi lần thu hoạch đúng loại berry.");
        return b.toString();
    }
}
