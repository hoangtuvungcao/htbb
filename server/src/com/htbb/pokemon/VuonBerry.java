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
 *  - Hat giong: CHI mua o vuon luc gieo, gia {@link #GIA_HAT_PHAN_TRAM}% gia tham chieu 1 berry (bac theo cap) - mot lan
 *    thu (3 berry) = 6 lan tien hat: trong la LOI (chu game 29/10: "lỗ thì người chơi chơi làm cái éo gì"). Berry trong tui
 *    KHONG gieo duoc (khong tu nhan ban mien phi).
 *  - Lon theo dong ho may chu ({@link DongHo#bay()}): {@link #PHUT} phut. Chin roi giu nguyen (khong heo), thu luc nao cung duoc.
 *  - Thu hoach: {@link #SAN_LUONG} berry (bac luc gieo) vao Tui sinh thai, +{@link #THEM_KHI_TUOI} neu hom nay vuon duoc
 *    ban be tuoi tu {@link #TUOI_DE_THEM} lan. Toi da {@link #THU_TOI_DA_NGAY} lan thu / ngay.
 *  - Gia tri that: che thuoc tu vuon (KyNangSong.CONG_THUC 30..34, chi berry) ra 10 binh HP / MP that re hon mua NPC
 *    ({@link #tietKiemMe}); nau Banh Poke (+30 the luc), sup berry, chau hoa (congDung).
 *  - Khong lam phat: ban NPC co the loi chut it nhung moi duong doi vat lieu sinh thai ra xu (HeSinhThaiMenu.banNpc, duy
 *    nhat) bi TRAN CHUNG 0,1 gio thu nhap / ngay (ChoSinhThai.tranBanNpcNgay, chung voi ca / khoang); cho nguoi choi =
 *    chuyen tien (thue 7% dot). Binh thuoc that khong ban NPC duoc -> tiet kiem thuoc chi la bot ho hut thuoc, toi da
 *    ME_THUOC_TOI_DA_NGAY me / ngay.
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
    /** Gia hat = % gia tham chieu 1 berry o bac theo cap. */
    public static final int GIA_HAT_PHAN_TRAM = 50;
    /** Gia NPC 1 binh thuoc that (data_potion price, Chansey / Kecleon; sua_gia_thuoc.sql 01/11/2026): 90 HP nho 2, 91 HP
     * vua 4, 92 HP lon 7, 93 MP nho 2, 94 MP vua 4. Test KiemVuonBerry so voi phan A sql/htbb.sql. Chi so = ma vat lieu - 90. */
    public static final int[] GIA_BINH_NPC = {2, 4, 7, 2, 4};
    /**
     * Trong so tran thuoc vuon / ngay (h.thuocVuonHomNay, {@link #tranThuocNgay}): gia NPC CU truoc 01/11/2026
     * (100 / 500 / 800 / 50 / 150). Giu nguyen de tran ngay van dung SO BINH nhu truoc khi gia NPC ha (khong che duoc nhieu
     * hon / ngay); so da luu trong ho so (cung don vi) van dung.
     */
    public static final int[] TRONG_SO_TRAN = {100, 500, 800, 50, 150};
    /** Tien that 1 me thuoc tu vuon (hat + bep) = % gia NPC cua 10 binh ra (chu game 01/11/2026: ~60-70%). */
    public static final int ME_VUON_PHAN_TRAM_NPC = 65;
    /** Phan cua tien hat trong tien 1 me thuoc vuon (%): hat bi kep de con >= 20% cho phi bep. */
    public static final int HAT_PHAN_TRAM_ME = 80;
    public static final String[] TEN_BINH = {"HP nhỏ", "HP vừa", "HP lớn", "MP nhỏ", "MP vừa"};
    /** Cong thuc thuoc tu vuon (KyNangSong.CONG_THUC). */
    public static final int CT_DAU = 30, CT_CUOI = 34;

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

    /**
     * Gia 1 hat (xu) o cap nguoi choi: {@link #GIA_HAT_PHAN_TRAM}% gia tham chieu 1 berry, KEP boi {@link #hatToiDa} (01/11/2026:
     * gia thuoc NPC ha 2-7 xu / binh - hat theo bac cap lam me thuoc vuon dat gap 10-300 lan mua NPC).
     */
    public static long giaHat(final int ma, final int cap) {
        final long goc = VatLieuSinhThai.giaXu(ma, VatLieuSinhThai.bacTheoCap(cap)) * GIA_HAT_PHAN_TRAM / 100L;
        return Math.max(1L, Math.min(goc, hatToiDa(ma)));
    }

    /** Tien that muc tieu 1 me thuoc vuon {@code ct} = ceil({@link #ME_VUON_PHAN_TRAM_NPC}% gia NPC 10 binh ra). */
    public static long meMucTieu(final KyNangSong.CongThuc ct) {
        return (giaNpcMe(ct) * ME_VUON_PHAN_TRAM_NPC + 99L) / 100L;
    }

    /**
     * Tran gia 1 hat berry {@code ma}, suy tu GIA_BINH_NPC: moi cong thuc thuoc vuon dung berry nay, tien hat cua mot me
     * (moi hat ra {@link #SAN_LUONG} berry) &lt;= {@link #HAT_PHAN_TRAM_ME}% {@link #meMucTieu}; lay min. Berry khong vao
     * thuoc vuon nao: khong kep.
     */
    public static long hatToiDa(final int ma) {
        long tran = Long.MAX_VALUE;
        for (final KyNangSong.CongThuc ct : KyNangSong.CONG_THUC) {
            if (!laThuocVuon(ct) || ct.ra < 90 || ct.ra > 94) {
                continue;
            }
            int soBerry = 0;
            boolean co = false;
            for (final int[] nl : ct.nguyenLieu) {
                soBerry += nl[1];
                co |= nl[0] == ma;
            }
            if (co && soBerry > 0) {
                tran = Math.min(tran, (long) SAN_LUONG * (meMucTieu(ct) * HAT_PHAN_TRAM_ME / 100L) / soBerry);
            }
        }
        return Math.max(1L, tran);
    }

    /** Tien hat cua 1 me {@code ct} o cap (moi hat ra SAN_LUONG berry, lam tron len). */
    public static long hatMe(final KyNangSong.CongThuc ct, final int cap) {
        long hat = 0;
        for (final int[] nl : ct.nguyenLieu) {
            hat += giaHat(nl[0], cap) * nl[1];
        }
        return (hat + SAN_LUONG - 1) / SAN_LUONG;
    }

    /** Phi bep 1 me thuoc vuon: phan con lai cua {@link #meMucTieu} sau tien hat (tong = muc tieu, khong theo cap). */
    public static long phiBepThuocVuon(final KyNangSong.CongThuc ct, final int cap) {
        return Math.max(0L, meMucTieu(ct) - hatMe(ct, cap));
    }

    /** Phi bep (xu) o cap: HeSinhThai.PHI_BEP_PHAN_NGHIN ‰ gio thu nhap (nhu HeSinhThaiGame.phi). */
    public static long phiBep(final int cap) {
        final int c = Math.max(1, Math.min(ThongSoLauDai.CAP_TOI_DA, cap));
        return Math.max(1L, ThongSoLauDai.xuMoiGio(c) * HeSinhThai.PHI_BEP_PHAN_NGHIN / 1000L);
    }

    /** Cong thuc chi dung berry gieo duoc va moi berry da mo o cap {@code cap}. */
    public static boolean meVuonDuoc(final KyNangSong.CongThuc ct, final int cap) {
        if (ct.ra < 90 || ct.ra > 94) {
            return false;
        }
        for (final int[] nl : ct.nguyenLieu) {
            final int i = chiSo(nl[0]);
            if (i < 0 || CAP[i] > cap) {
                return false;
            }
        }
        return true;
    }

    /** Tien xu that cua 1 me thuoc tu vuon o cap: hat (moi hat ra SAN_LUONG berry) + phi bep. */
    public static long chiPhiMe(final KyNangSong.CongThuc ct, final int cap) {
        return hatMe(ct, cap) + (laThuocVuon(ct) ? phiBepThuocVuon(ct, cap) : phiBep(cap));
    }

    /** Trong so tran ngay cua 1 me (gia NPC cu 10 binh - {@link #TRONG_SO_TRAN}). */
    public static long trongSoMe(final KyNangSong.CongThuc ct) {
        return 10L * TRONG_SO_TRAN[ct.ra - 90];
    }

    /** Gia NPC cua 10 binh (1 me). */
    public static long giaNpcMe(final KyNangSong.CongThuc ct) {
        return 10L * GIA_BINH_NPC[ct.ra - 90];
    }

    /** Tiet kiem 1 me so voi mua NPC (co the am o cap cao voi binh re). */
    public static long tietKiemMe(final KyNangSong.CongThuc ct, final int cap) {
        return giaNpcMe(ct) - chiPhiMe(ct, cap);
    }

    /**
     * Tran gia tri NPC cua thuoc tu vuon / ngay = ngan sach thuoc thiet ke cua nguoi choi {@link #GIO_NGAN_SACH_THUOC} gio
     * (ThongSoLauDai.TIEU_HAO_PHAN_TRAM % thu nhap moi gio): vuon chi thay toi da phan thuoc cua mot nguoi choi vua (3 gio),
     * nguoi choi nhieu van mua phan con lai - ho hut thuoc khong mat.
     */
    public static final int GIO_NGAN_SACH_THUOC = 3;

    public static long tranThuocNgay(final int cap) {
        final int c = Math.max(1, Math.min(ThongSoLauDai.CAP_TOI_DA, cap));
        return GIO_NGAN_SACH_THUOC * ThongSoLauDai.xuMoiGio(c) * ThongSoLauDai.TIEU_HAO_PHAN_TRAM / 100L;
    }

    public static boolean laThuocVuon(final KyNangSong.CongThuc ct) {
        return ct.id >= CT_DAU && ct.id <= CT_CUOI;
    }

    /**
     * Tiet kiem toi da / ngay o cap (bo qua cap ky nang Che thuoc): chon so me moi cong thuc, tong <= ME_THUOC_TOI_DA_NGAY va
     * tong trong so tran ({@link #trongSoMe}) <= {@link #tranThuocNgay}, lon nhat tong (gia NPC - tien that).
     */
    public static long tietKiemNgay(final int cap) {
        final java.util.List<KyNangSong.CongThuc> ds = new java.util.ArrayList<KyNangSong.CongThuc>();
        for (final KyNangSong.CongThuc ct : KyNangSong.CONG_THUC) {
            if (laThuocVuon(ct) && meVuonDuoc(ct, cap) && tietKiemMe(ct, cap) > 0) {
                ds.add(ct);
            }
        }
        return tot(ds, 0, KyNangSong.ME_THUOC_TOI_DA_NGAY, tranThuocNgay(cap), cap);
    }

    private static long tot(final java.util.List<KyNangSong.CongThuc> ds, final int i, final int me, final long tien, final int cap) {
        if (i >= ds.size() || me <= 0) {
            return 0L;
        }
        long best = tot(ds, i + 1, me, tien, cap);
        final KyNangSong.CongThuc ct = ds.get(i);
        if (trongSoMe(ct) <= tien) {
            best = Math.max(best, tietKiemMe(ct, cap) + tot(ds, i, me - 1, tien - trongSoMe(ct), cap));
        }
        return best;
    }

    /** Xu toi da / ngay ban NPC tu vuon (sau tien hat): min(12 lan x (4 berry x gia NPC - hat), tran chung NPC). */
    public static long xuNpcNgayToiDa(final int cap) {
        long tot = 0;
        for (final int ma : MA) {
            if (CAP[chiSo(ma)] <= cap) {
                final long loi = sanLuong(99) * VatLieuSinhThai.giaNpc(ma, VatLieuSinhThai.bacTheoCap(cap)) - giaHat(ma, cap);
                tot = Math.max(tot, loi * THU_TOI_DA_NGAY);
            }
        }
        return Math.min(Math.max(0L, tot), ChoSinhThai.tranBanNpcNgay(cap));
    }

    /** Ten cac cong thuc dung berry {@code ma} (uu tien thuoc tu vuon), toi da {@code n}. */
    public static String congDung(final int ma, final int n) {
        final java.util.List<String> l = new java.util.ArrayList<String>();
        for (int k = 0; k < 2; k++) {
            for (final KyNangSong.CongThuc ct : KyNangSong.CONG_THUC) {
                final boolean vuon = ct.id >= CT_DAU && ct.id <= CT_CUOI;
                if (vuon != (k == 0) || l.size() >= n) {
                    continue;
                }
                for (final int[] nl : ct.nguyenLieu) {
                    if (nl[0] == ma && !l.contains(ct.ten)) {
                        l.add(ct.ten);
                    }
                }
            }
        }
        return String.join(", ", l);
    }

    /** Chu "berry dung lam gi" (menu Hướng dẫn trồng berry, sau thu hoach). */
    public static String moTaCongDung(final int cap) {
        final StringBuilder b = new StringBuilder("Dùng berry: Hệ sinh thái → Nấu ăn, chế thuốc, chế tác.\n");
        for (final KyNangSong.CongThuc ct : KyNangSong.CONG_THUC) {
            if (ct.id >= CT_DAU && ct.id <= CT_CUOI) {
                b.append("- ").append(ct.ten).append(" (Chế thuốc ").append(ct.capToiThieu).append("): ");
                for (int i = 0; i < ct.nguyenLieu.length; i++) {
                    b.append(i == 0 ? "" : " + ").append(ct.nguyenLieu[i][1]).append(' ').append(ten(ct.nguyenLieu[i][0]));
                }
                b.append(" → 10 bình ").append(TEN_BINH[ct.ra - 90]).append(" (NPC bán ").append(giaNpcMe(ct)).append(" xu");
                if (meVuonDuoc(ct, cap)) {
                    b.append(", tự trồng tốn ~").append(chiPhiMe(ct, cap)).append(" xu");
                }
                b.append(")\n");
            }
        }
        b.append("- Bánh Poké: 2 Berry Oran + 1 Berry Pecha (+30 thể lực sinh hoạt); Súp berry: 3 Berry Rawst + 1 Rong biển "
                + "(EXP kỹ năng x1,5); Chậu hoa berry: trang trí vườn. Tối đa ").append(KyNangSong.ME_THUOC_TOI_DA_NGAY)
                .append(" mẻ thuốc / ngày; thuốc từ vườn tối đa ").append(tranThuocNgay(cap)).append(" điểm / ngày (bình HP nhỏ 1.000 / mẻ).");
        return b.toString();
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
                + " tưới từ " + TUOI_DE_THEM + " lần). Tối đa " + THU_TOI_DA_NGAY + " lần thu hoạch mỗi ngày. Hạt rẻ: một lần"
                + " thu được gấp 6 lần tiền hạt.\n");
        for (int i = 0; i < MA.length; i++) {
            b.append("- ").append(ten(MA[i])).append(": cấp ").append(CAP[i]).append(", ").append(PHUT[i]).append(" phút\n");
        }
        b.append("Ô đất: ").append(O_CO_BAN).append(" ô, thêm 1 ô ở cấp ").append(CAP_THEM_O[0]).append(" và cấp ")
                .append(CAP_THEM_O[1]).append(", mua thêm tối đa ").append(O_MUA_TOI_DA).append(" ô bằng xu (tối đa ")
                .append(O_TOI_DA).append(" ô). Nhiệm vụ \"Trồng và thu hoạch\" ở Sổ tay tính mỗi lần thu hoạch đúng loại berry.");
        return b.toString();
    }
}
