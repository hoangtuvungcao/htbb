package com.htbb.pokemon;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

/**
 * "Bo nao" cua Tu dong danh chinh thuc: MAY CHU quyet dinh moi buoc (danh con nao, chieu nao, uong thuoc gi, nhat
 * mon nao, rut lui / dung), client chi thuc hien bang dung ham nhu nguoi bam tay (GameScr.doUseSkillToFocusActor,
 * doUsePotion, getDropableFromGround). Lop THUAN (khong Char / Map / CSDL): real.TuDongGame dung {@link TinhHinh} tu
 * trang thai that roi goi {@link #lap}; test/KiemTuDongKeHoach.java goi truc tiep.
 *
 * Thu tu uu tien moi buoc (tu_dong.* trong tools/cauhinh/danh_muc.json):
 *   1. tui day (tu_dong.dung_khi_day_tui) -> DUNG;
 *   2. HP / PP duoi nguong (nguoi choi chon; tu_dong.nguong_thuoc_* la mac dinh, 0 = may chu tat) va con thuoc ->
 *      THUOC (binh nho nhat du bu phan thieu, khong phi binh lon);
 *   3. HP duoi tu_dong.nguong_hp_thap ma khong con thuoc -> RUT LUI ve diem bat dau nghi toi tu_dong.hoi_phuc_toi %
 *      (hoac DUNG neu tu_dong.hp_thap_dung=1 / nguoi choi tat "Ve hoi phuc");
 *   4. nguoi choi khac bat PK o gan (tu_dong.tranh_pk_px) -> lui ve diem bat dau, cho;
 *   5. do roi cua minh trong tu_dong.nhat_ban_kinh (loc theo kieu nhat cua nguoi choi) -> NHAT;
 *   6. muc tieu: Pokemon hoang da GAN NHAT THEO DUONG DI (BFS tren o di duoc, khong xuyen tuong) trong vung, dung
 *      khoang cap (tu_dong.chenh_cap_tren / _duoi), khong boss (tu_dong.danh_boss), khong qua manh
 *      (tu_dong.hp_quai_toi_da_pt); giu muc tieu cu neu con hop le (khong nhay qua lai);
 *   7. chieu: diem = sat thuong % x he so khac he (HePokemon / ChieuPokemon, tu_dong.chon_chieu_theo_he) x STAB, chi
 *      chieu da hoc, het hoi chieu, du PP; khong co chieu nao san thi DANH voi chieu -1 (client tien lai, cho);
 *   8. khong co muc tieu: ve diem bat dau / cho, kem LY DO ro rang (bao nguoi choi).
 */
public final class TuDongKeHoach {

    private TuDongKeHoach() {
    }

    // ================================================================== hanh dong (goi -100 loai 1, server -> client)

    public static final int HD_CHO = 0, HD_DANH = 1, HD_THUOC = 2, HD_NHAT = 3, HD_VE = 4, HD_DUNG = 5;

    /** Kieu nhat cua nguoi choi (khop client TuDong.nhat): 0 khong, 1 thuoc + xu, 2 them da / nguyen lieu, 3 tat ca. */
    public static final int NHAT_KHONG = 0, NHAT_THUOC_XU = 1, NHAT_DA = 2, NHAT_TAT_CA = 3;
    /** Loai do roi (Actor.cat cua game): 3 vat pham, 4 thuoc / xu, 6-7 ngoc / da. */
    public static final int DO_VAT_PHAM = 3, DO_THUOC = 4;

    /** Binh HP {loai, hoi} yeu -> manh (real.doUsePotion: 1-3 theo data_potion, 21/22/93/94 co dinh). */
    public static final int[][] BINH_HP = {{1, 80}, {2, 300}, {3, 1000}, {21, 1500}, {22, 3000}, {93, 7000}, {94, 15000}};
    /** Binh PP (MP) {loai, hoi} yeu -> manh. */
    public static final int[][] BINH_MP = {{4, 160}, {5, 600}, {6, 2000}, {23, 2500}, {24, 3500}, {95, 7000}, {96, 15000}};

    // ================================================================== cau hinh (khop danh_muc.json)

    public static final int CHENH_CAP_TREN_MAC_DINH = 5;
    public static final int CHENH_CAP_DUOI_MAC_DINH = 0;
    public static final int HP_QUAI_TOI_DA_PT_MAC_DINH = 500;
    public static final int NGUONG_THUOC_HP_MAC_DINH = 50;
    public static final int NGUONG_THUOC_MP_MAC_DINH = 30;
    public static final int NGUONG_HP_THAP_MAC_DINH = 25;
    public static final int HOI_PHUC_TOI_MAC_DINH = 80;
    public static final int NHAT_BAN_KINH_MAC_DINH = 96;
    public static final int TRANH_PK_PX_MAC_DINH = 160;
    /** Gioi han tim duong (o) - BFS khong vuot qua ngan nay o moi chieu quanh nguoi choi. */
    public static final int TIM_DUONG_O = 40;
    /**
     * Khoang toi thieu giua hai lan bo nao cho uong thuoc (ms): duoi nhip tu_dong.nhip_thuoc mac dinh 4 / 5 giay de
     * khong bao gio tu tinh vi pham chong bot.
     */
    public static final long KHOANG_THUOC_MS = 1500L;
    /** Lech toi da (px) giua nguoi choi va diem bat dau thi coi la "da ve cho". */
    public static final int VE_CHO_PX = 24;

    public static final class CauHinh {
        public int chenhCapTren = CHENH_CAP_TREN_MAC_DINH;
        public int chenhCapDuoi = CHENH_CAP_DUOI_MAC_DINH;
        public boolean danhBoss;
        /** 0 = tat; > 0: bo qua Pokemon co HP toi da > ngan nay % HP toi da cua minh. */
        public int hpQuaiToiDaPt = HP_QUAI_TOI_DA_PT_MAC_DINH;
        public boolean chonChieuTheoHe = true;
        public boolean tuDungThuoc = true;
        /** Tran tren cua nguong thuoc nguoi choi chon (%). */
        public int nguongThuocHp = NGUONG_THUOC_HP_MAC_DINH;
        public int nguongThuocMp = NGUONG_THUOC_MP_MAC_DINH;
        public int nguongHpThap = NGUONG_HP_THAP_MAC_DINH;
        /** true = HP thap ma het thuoc thi DUNG han; false = rut lui ve diem bat dau nghi. */
        public boolean hpThapDung;
        public int hoiPhucToi = HOI_PHUC_TOI_MAC_DINH;
        public int nhatBanKinh = NHAT_BAN_KINH_MAC_DINH;
        public boolean dungKhiDayTui = true;
        /** 0 = tat; > 0: nguoi choi khac dang bat PK trong ban kinh nay (px) -> rut ve, cho. */
        public int tranhPkPx = TRANH_PK_PX_MAC_DINH;
    }

    // ================================================================== du lieu dau vao

    /** Pokemon hoang da (quai) tren ban do, cung phe / vung voi nguoi choi. */
    public static final class Quai {
        public int id, x, y, hp, maxhp, cap, he1 = -1, he2 = -1;
        public boolean boss;
        /** Quai cua nguoi khac (xe giao hang, quai trieu hoi...) - khong danh. */
        public boolean cuaNguoiKhac;

        public Quai() {
        }

        public Quai(final int id, final int x, final int y, final int cap, final int hp, final int maxhp) {
            this.id = id;
            this.x = x;
            this.y = y;
            this.cap = cap;
            this.hp = hp;
            this.maxhp = maxhp;
        }
    }

    /** Mot chieu danh duoc (da hoc, khong phai chieu buff). */
    public static final class Chieu {
        /** So hieu skill (byte gui lenh danh 9). */
        public int so;
        /** He chieu 0..17, -1 = khong ro (dung he 1 cua minh, khong STAB). */
        public int he = -1;
        /** Sat thuong % (CharManager.SKILL_DAM_PERCENT). */
        public int satThuong = 100;
        public int pp;
        /** Mili-giay con lai truoc khi dung lai duoc (0 = san sang). */
        public long conHoi;

        public Chieu() {
        }

        public Chieu(final int so, final int he, final int satThuong, final int pp, final long conHoi) {
            this.so = so;
            this.he = he;
            this.satThuong = satThuong;
            this.pp = pp;
            this.conHoi = conHoi;
        }
    }

    /** Do roi tren dat. */
    public static final class DoRoi {
        public int cat, id, x, y;
        /** Loai thuoc (cat 4): 0 = xu. */
        public int loai;
        /** Cua minh hoac khong chu (nguoi khac danh roi thi khong nhat duoc trong thoi gian giu do). */
        public boolean nhatDuoc = true;

        public DoRoi() {
        }

        public DoRoi(final int cat, final int id, final int x, final int y, final int loai) {
            this.cat = cat;
            this.id = id;
            this.x = x;
            this.y = y;
            this.loai = loai;
        }
    }

    /** Kiem o di duoc (toa do PX) - game: Map.canMove, test: lambda. */
    public interface DiDuoc {
        boolean o(int x, int y);
    }

    /** Anh chup tinh hinh cua MOT nhan vat luc lap ke hoach. */
    public static final class TinhHinh {
        public int x, y, cap, hp, maxhp, mp, maxmp;
        public int he1 = -1, he2 = -1;
        public int neoX, neoY, banKinh = TuDong.BAN_KINH_MAC_DINH;
        public final List<Quai> quai = new ArrayList<Quai>();
        public final List<Chieu> chieu = new ArrayList<Chieu>();
        public final List<DoRoi> doRoi = new ArrayList<DoRoi>();
        /** So luong theo loai thuoc (Char.potions). */
        public int[] thuoc = new int[0];
        /** Mili-giay con lai truoc khi uong lai duoc theo loai thuoc (null = san sang het). */
        public long[] thuocConHoi;
        /** Hoi cua binh HP 1..3 doc tu data_potion (null = mac dinh BINH_HP). */
        public int[] hoiBinh;
        public boolean tuiDay;
        /** So nguoi choi khac dang bat PK trong tu_dong.tranh_pk_px. */
        public int nguoiPkGan;
        /** Cai dat cua nguoi choi (client gui khi bat; client cu = mac dinh may chu). */
        public int nguongHpNguoi = -1, nguongMpNguoi = -1, nhatNguoi = NHAT_THUOC_XU;
        public boolean veHoiPhucNguoi = true;
        /** Muc tieu buoc truoc (-1 = khong) - giu de khong nhay qua lai. */
        public int mucTieuCu = -1;
        /** Dang rut lui hoi phuc tu buoc truoc. */
        public boolean dangHoiPhuc;
        /** Thuoc vua dung (ms truoc) - tranh uong lien tiep khi goi tin cap nhat HP chua toi. */
        public long tuLanThuocMs = Long.MAX_VALUE;
        public DiDuoc diDuoc;
    }

    /** Ke hoach mot buoc. */
    public static final class KeHoach {
        public int hd = HD_CHO;
        /** Muc tieu (id quai / id do roi), -1 = khong. */
        public int id = -1;
        /** Chieu (HD_DANH), loai thuoc (HD_THUOC), cat do roi (HD_NHAT). */
        public int so = -1;
        /** Diem den (px). */
        public int x, y;
        /** Ly do / trang thai cho nguoi choi (thanh trang thai + bao). */
        public String ly = "";
        /** HD_DUNG: ma TuDong.TAT_*. */
        public int lyDoTat = -1;
        /** He so khac he cua chieu da chon (100 = thuong) - de ghi trang thai "hiệu quả". */
        public int heSo = 100;
        /** Dang rut lui hoi phuc (luu sang buoc sau). */
        public boolean hoiPhuc;

        @Override
        public String toString() {
            return "hd=" + hd + " id=" + id + " so=" + so + " @" + x + "," + y + " heSo=" + heSo + " '" + ly + "'";
        }
    }

    // ================================================================== lap ke hoach

    public static KeHoach lap(final CauHinh c, final TinhHinh t) {
        final KeHoach k = new KeHoach();
        k.x = t.neoX;
        k.y = t.neoY;
        // 1. tui day
        if (c.dungKhiDayTui && t.tuiDay) {
            k.hd = HD_DUNG;
            k.lyDoTat = TuDong.TAT_DAY_TUI;
            k.ly = TuDong.lyDo(TuDong.TAT_DAY_TUI);
            return k;
        }
        final int pt = phanTram(t.hp, t.maxhp);
        // 3. thuoc (uu tien truoc PK: dang mat mau thi uong truoc da)
        if (c.tuDungThuoc && t.tuLanThuocMs >= KHOANG_THUOC_MS) {
            final int nguongHp = nguong(t.nguongHpNguoi, c.nguongThuocHp);
            if (nguongHp > 0 && pt < nguongHp) {
                final int loai = chonBinh(t, BINH_HP, t.maxhp - t.hp);
                if (loai >= 0) {
                    k.hd = HD_THUOC;
                    k.so = loai;
                    k.ly = "uống thuốc HP (" + pt + "%)";
                    k.hoiPhuc = t.dangHoiPhuc;
                    return k;
                }
            }
            final int ptMp = phanTram(t.mp, t.maxmp);
            final int nguongMp = nguong(t.nguongMpNguoi, c.nguongThuocMp);
            if (nguongMp > 0 && t.maxmp > 0 && ptMp < nguongMp) {
                final int loai = chonBinh(t, BINH_MP, t.maxmp - t.mp);
                if (loai >= 0) {
                    k.hd = HD_THUOC;
                    k.so = loai;
                    k.ly = "uống thuốc PP (" + ptMp + "%)";
                    k.hoiPhuc = t.dangHoiPhuc;
                    return k;
                }
            }
        }
        // 4. HP thap, het thuoc: rut lui / dung
        final boolean thap = c.nguongHpThap > 0 && pt < c.nguongHpThap;
        boolean hoiPhuc = t.dangHoiPhuc && t.veHoiPhucNguoi && pt < Math.max(c.nguongHpThap, c.hoiPhucToi);
        if (thap && !hoiPhuc) {
            if (c.hpThapDung || !t.veHoiPhucNguoi) {
                k.hd = HD_DUNG;
                k.lyDoTat = TuDong.TAT_HP_THAP;
                k.ly = TuDong.lyDo(TuDong.TAT_HP_THAP);
                return k;
            }
            hoiPhuc = true;
        }
        if (hoiPhuc) {
            k.hoiPhuc = true;
            veCho(t, k, "về nghỉ hồi phục " + pt + "% (hết thuốc)", "nghỉ hồi phục " + pt + "%");
            return k;
        }
        // 2. PK gan
        if (c.tranhPkPx > 0 && t.nguoiPkGan > 0) {
            veCho(t, k, "có người bật PK gần đây - lùi về điểm bắt đầu", "có người bật PK gần đây - tạm chờ");
            return k;
        }
        // 5. nhat do
        final DoRoi d = chonDo(c, t);
        if (d != null) {
            k.hd = HD_NHAT;
            k.id = d.id;
            k.so = d.cat;
            k.x = d.x;
            k.y = d.y;
            k.ly = "nhặt đồ";
            return k;
        }
        // 6. muc tieu
        final int[] lyBo = new int[5];
        lyBo[GAN_NGOAI_VUNG] = Integer.MAX_VALUE;
        final Quai q = chonQuai(c, t, lyBo);
        if (q == null) {
            veCho(t, k, "về điểm bắt đầu", lyDoKhongCoQuai(c, t, lyBo));
            return k;
        }
        k.hd = HD_DANH;
        k.id = q.id;
        k.x = q.x;
        k.y = q.y;
        // 7. chieu
        final Chieu ch = chonChieu(c, t, q);
        if (ch == null) {
            k.so = -1;
            k.ly = "chờ hồi chiêu / PP";
            return k;
        }
        k.so = ch.so;
        k.heSo = heSoChieu(c, t, ch, q);
        k.ly = k.heSo >= 150 ? "đánh - chiêu rất hiệu quả" : (k.heSo < 100 ? "đánh - chiêu kém hiệu quả" : "đang đánh");
        return k;
    }

    static void veCho(final TinhHinh t, final KeHoach k, final String khiDi, final String khiToi) {
        k.x = t.neoX;
        k.y = t.neoY;
        if (khoang2(t.x, t.y, t.neoX, t.neoY) > (long) VE_CHO_PX * VE_CHO_PX) {
            k.hd = HD_VE;
            k.ly = khiDi;
        } else {
            k.hd = HD_CHO;
            k.ly = khiToi;
        }
    }

    /**
     * Nguong thuc te: may chu dat 0 = tat han; client cu (khong gui, -1) = mac dinh may chu; client moi = nguong nguoi
     * choi chon trong bang (0 = nguoi choi tat), toi da 90%.
     */
    static int nguong(final int nguoi, final int macDinh) {
        if (macDinh <= 0) {
            return 0;
        }
        if (nguoi < 0) {
            return macDinh;
        }
        return Math.min(nguoi, 90);
    }

    static int phanTram(final int a, final int b) {
        return b <= 0 ? 100 : (int) Math.max(0L, Math.min(100L, (long) a * 100L / b));
    }

    static long khoang2(final int x1, final int y1, final int x2, final int y2) {
        final long dx = x1 - x2, dy = y1 - y2;
        return dx * dx + dy * dy;
    }

    // ------------------------------------------------------------------ thuoc

    /**
     * Binh nho nhat du bu {@code thieu}; khong co binh nao du thi binh manh nhat dang co. Chi binh dang co va het hoi.
     * @return loai thuoc, -1 = khong co.
     */
    static int chonBinh(final TinhHinh t, final int[][] bang, final int thieu) {
        int vua = -1, manh = -1;
        for (int i = 0; i < bang.length; i++) {
            final int loai = bang[i][0];
            if (loai >= t.thuoc.length || t.thuoc[loai] <= 0) {
                continue;
            }
            if (t.thuocConHoi != null && loai < t.thuocConHoi.length && t.thuocConHoi[loai] > 0L) {
                continue;
            }
            final int hoi = hoi(t, bang, i);
            manh = loai;
            if (vua < 0 && hoi >= thieu) {
                vua = loai;
            }
        }
        return vua >= 0 ? vua : manh;
    }

    static int hoi(final TinhHinh t, final int[][] bang, final int i) {
        if (bang == BINH_HP && t.hoiBinh != null && i < 3 && i < t.hoiBinh.length && t.hoiBinh[i] > 0) {
            return t.hoiBinh[i];
        }
        return bang[i][1];
    }

    // ------------------------------------------------------------------ do roi

    static boolean muonNhat(final int kieu, final DoRoi d, final boolean tuiDay) {
        if (kieu <= NHAT_KHONG || !d.nhatDuoc) {
            return false;
        }
        if (d.cat == DO_THUOC) {
            return true; // thuoc / xu: kieu >= 1 (thuoc chong, khong can o tui moi neu da co loai do)
        }
        if (d.cat == DO_VAT_PHAM) {
            return kieu >= NHAT_TAT_CA && !tuiDay;
        }
        return kieu >= NHAT_DA && !tuiDay;
    }

    static DoRoi chonDo(final CauHinh c, final TinhHinh t) {
        if (c.nhatBanKinh <= 0) {
            return null;
        }
        final long r2 = (long) c.nhatBanKinh * c.nhatBanKinh;
        final long vung2 = (long) t.banKinh * t.banKinh;
        DoRoi tot = null;
        long gan = Long.MAX_VALUE;
        for (final DoRoi d : t.doRoi) {
            if (!muonNhat(t.nhatNguoi, d, t.tuiDay)) {
                continue;
            }
            final long k = khoang2(t.x, t.y, d.x, d.y);
            if (k > r2 || khoang2(t.neoX, t.neoY, d.x, d.y) > vung2) {
                continue;
            }
            if (k < gan) {
                gan = k;
                tot = d;
            }
        }
        return tot;
    }

    // ------------------------------------------------------------------ muc tieu

    /** Ly do bo qua (dem): 0 ngoai vung, 1 manh hon qua nhieu / ngoai khoang cap, 2 boss, 3 khong toi duoc. */
    static final int BO_NGOAI_VUNG = 0, BO_CAP = 1, BO_BOSS = 2, BO_DUONG = 3;
    /** O thu 5 cua mang ly do: khoang cach (px, tu diem bat dau) toi Pokemon hop le gan nhat NGOAI vung. */
    static final int GAN_NGOAI_VUNG = 4;

    /**
     * Hop le theo luat (chua tinh duong di). -1 = hop le, nguoc lai ma BO_*. Vung kiem SAU cung: "ngoai vung" chi
     * dem nhung con hop le khac (de bao nguoi choi con gan nhat cach bao xa).
     */
    static int lyDoBo(final CauHinh c, final TinhHinh t, final Quai q) {
        if (q.boss && !c.danhBoss) {
            return BO_BOSS;
        }
        if (c.chenhCapTren >= 0 && q.cap > t.cap + c.chenhCapTren) {
            return BO_CAP;
        }
        if (c.chenhCapDuoi > 0 && q.cap < t.cap - c.chenhCapDuoi) {
            return BO_CAP;
        }
        if (c.hpQuaiToiDaPt > 0 && t.maxhp > 0 && (long) q.maxhp * 100L > (long) t.maxhp * c.hpQuaiToiDaPt) {
            return BO_CAP;
        }
        if (khoang2(t.neoX, t.neoY, q.x, q.y) > (long) t.banKinh * t.banKinh) {
            return BO_NGOAI_VUNG;
        }
        return -1;
    }

    static Quai chonQuai(final CauHinh c, final TinhHinh t, final int[] lyBo) {
        final List<Quai> hopLe = new ArrayList<Quai>();
        Quai cu = null;
        for (final Quai q : t.quai) {
            if (q == null || q.hp <= 0 || q.cuaNguoiKhac) {
                continue;
            }
            final int bo = lyDoBo(c, t, q);
            if (bo >= 0) {
                lyBo[bo]++;
                if (bo == BO_NGOAI_VUNG) {
                    lyBo[GAN_NGOAI_VUNG] = (int) Math.min(lyBo[GAN_NGOAI_VUNG], Math.sqrt((double) khoang2(t.neoX, t.neoY, q.x, q.y)));
                }
                continue;
            }
            hopLe.add(q);
            if (q.id == t.mucTieuCu) {
                cu = q;
            }
        }
        if (hopLe.isEmpty()) {
            return null;
        }
        final int[] buoc = duongDi(t, hopLe);
        if (cu != null && buoc[hopLe.indexOf(cu)] >= 0) {
            return cu; // giu muc tieu dang danh
        }
        Quai tot = null;
        int gan = Integer.MAX_VALUE;
        for (int i = 0; i < hopLe.size(); i++) {
            final int b = buoc[i];
            if (b < 0) {
                lyBo[BO_DUONG]++;
                continue;
            }
            final Quai q = hopLe.get(i);
            // cung khoang duong: con it mau hon truoc (ha nhanh, it bi danh tra)
            if (b < gan || b == gan && tot != null && q.hp < tot.hp) {
                gan = b;
                tot = q;
            }
        }
        return tot;
    }

    static String lyDoKhongCoQuai(final CauHinh c, final TinhHinh t, final int[] lyBo) {
        final int o = Math.max(1, t.banKinh / 16);
        if (lyBo[BO_DUONG] > 0) {
            return "không tới được Pokémon nào trong vùng " + o + " ô (bị chắn)";
        }
        if (lyBo[BO_NGOAI_VUNG] > 0 && lyBo[GAN_NGOAI_VUNG] < Integer.MAX_VALUE) {
            return "Pokémon gần nhất cách " + Math.max(o + 1, (lyBo[GAN_NGOAI_VUNG] + 15) / 16) + " ô, ngoài vùng " + o
                    + " ô - đứng gần bãi hơn hoặc tăng Vùng trong bảng";
        }
        if (lyBo[BO_CAP] > 0 && lyBo[BO_CAP] >= lyBo[BO_BOSS]) {
            return "Pokémon quanh đây mạnh hơn quá nhiều (quá " + c.chenhCapTren + " cấp) - đổi bãi luyện tập";
        }
        if (lyBo[BO_BOSS] > 0) {
            return "chỉ có boss quanh đây - Tự động không đánh boss";
        }
        return "chờ Pokémon hoang dã trong vùng " + o + " ô";
    }

    /**
     * So buoc (o 16 px, 8 huong) tu nguoi choi toi tung quai bang MOT lan BFS; -1 = khong toi duoc trong
     * {@link #TIM_DUONG_O} o. Khong co DiDuoc (test don gian / ban do chua nap) = khoang cach Chebyshev.
     */
    static int[] duongDi(final TinhHinh t, final List<Quai> ds) {
        final int[] kq = new int[ds.size()];
        final int sx = t.x / 16, sy = t.y / 16;
        if (t.diDuoc == null) {
            for (int i = 0; i < ds.size(); i++) {
                kq[i] = Math.max(Math.abs(ds.get(i).x / 16 - sx), Math.abs(ds.get(i).y / 16 - sy));
            }
            return kq;
        }
        final int r = TIM_DUONG_O, w = 2 * r + 1;
        final int[] dist = new int[w * w];
        java.util.Arrays.fill(dist, -1);
        for (int i = 0; i < kq.length; i++) {
            kq[i] = -1;
        }
        final ArrayDeque<int[]> hang = new ArrayDeque<int[]>();
        dist[r * w + r] = 0;
        hang.add(new int[]{sx, sy});
        int conTim = ds.size();
        final int[][] huong = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}, {1, 1}, {1, -1}, {-1, 1}, {-1, -1}};
        while (!hang.isEmpty() && conTim > 0) {
            final int[] o = hang.poll();
            final int d = dist[(o[1] - sy + r) * w + (o[0] - sx + r)];
            for (int i = 0; i < ds.size(); i++) {
                if (kq[i] < 0) {
                    final Quai q = ds.get(i);
                    // quai dung tren o chan (bay, nuoc) van danh duoc tu o ke ben: tinh khi toi o ke
                    if (Math.abs(q.x / 16 - o[0]) <= 1 && Math.abs(q.y / 16 - o[1]) <= 1) {
                        kq[i] = d + (q.x / 16 == o[0] && q.y / 16 == o[1] ? 0 : 1);
                        conTim--;
                    }
                }
            }
            for (final int[] h : huong) {
                final int nx = o[0] + h[0], ny = o[1] + h[1];
                if (Math.abs(nx - sx) > r || Math.abs(ny - sy) > r || nx < 0 || ny < 0) {
                    continue;
                }
                final int idx = (ny - sy + r) * w + (nx - sx + r);
                if (dist[idx] >= 0) {
                    continue;
                }
                if (!t.diDuoc.o(nx * 16 + 8, ny * 16 + 8)) {
                    continue;
                }
                // di cheo khong cat goc tuong
                if (h[0] != 0 && h[1] != 0 && (!t.diDuoc.o((o[0] + h[0]) * 16 + 8, o[1] * 16 + 8) || !t.diDuoc.o(o[0] * 16 + 8, (o[1] + h[1]) * 16 + 8))) {
                    continue;
                }
                dist[idx] = d + 1;
                hang.add(new int[]{nx, ny});
            }
        }
        return kq;
    }

    // ------------------------------------------------------------------ chieu

    static int heSoChieu(final CauHinh c, final TinhHinh t, final Chieu ch, final Quai q) {
        if (!c.chonChieuTheoHe) {
            return 100;
        }
        final int heC = ch.he >= 0 ? ch.he : t.he1;
        if (!HePokemon.hopLe(heC) || !HePokemon.hopLe(q.he1)) {
            return 100;
        }
        return HePokemon.heSoTrongGame(heC, q.he1, q.he2);
    }

    /** Diem ky vong cua chieu vao quai: sat thuong % x khac he x STAB (/10000). */
    static long diem(final CauHinh c, final TinhHinh t, final Chieu ch, final Quai q) {
        final int heSo = heSoChieu(c, t, ch, q);
        final int stab = c.chonChieuTheoHe && ch.he >= 0 ? ChieuPokemon.heSoStab(ch.he, t.he1, t.he2) : 100;
        return (long) Math.max(1, ch.satThuong) * heSo * stab;
    }

    /**
     * Chieu tot nhat dang dung duoc (het hoi, du PP). Bang diem: uu tien chieu it PP hon (giu PP cho chieu manh khi
     * quai gan het mau: neu chieu yeu da du ha thi khong phi chieu manh).
     */
    static Chieu chonChieu(final CauHinh c, final TinhHinh t, final Quai q) {
        Chieu tot = null;
        long diemTot = -1L;
        for (final Chieu ch : t.chieu) {
            if (ch.conHoi > 0L || ch.pp > t.mp) {
                continue;
            }
            final long d = diem(c, t, ch, q);
            if (d > diemTot || d == diemTot && tot != null && ch.pp < tot.pp) {
                diemTot = d;
                tot = ch;
            }
        }
        return tot;
    }
}
