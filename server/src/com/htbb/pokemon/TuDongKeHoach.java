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
 * Thu tu uu tien moi buoc (tu_dong.* trong tools/luu_tru_sinh_du_lieu/cauhinh/danh_muc.json):
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
 *   7. chieu: CHI chieu nguoi choi da gan (TinhHinh.chieuGan: trang chieu tu dong dung / "Thu tu chieu"; chua gan
 *      chieu danh nao = moi chieu da hoc), het hoi chieu, du PP. "Thu tu chieu" tu dat: chieu dau tien dung duoc theo
 *      dung thu tu (bo chieu mien nhiem neu con chieu khac); trang chieu: diem = sat thuong % x he so khac he
 *      (HePokemon / ChieuPokemon, tu_dong.chon_chieu_theo_he) x STAB giua cac chieu DA GAN, bang diem giu thu tu o.
 *      Khong chieu da gan nao dung duoc thi danh thuong (chieu 0) neu san; khong co gi thi DANH voi chieu -1 (client
 *      tien lai, cho);
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
    /**
     * 12/11/2026 (bao loi "dang treo con nua cay mau ma chet"): truoc day MOT moc chung {@link TinhHinh#tuLanThuocMs} - dat
     * luc bo nao RA LENH thuoc (ca binh PP) - chan moi binh 1,5 giay: vua ra lenh binh PP (hoac client bo qua lenh vi vua tu
     * uong PP) la binh HP bi hoan 1,5-3 giay dung luc dang mat mau; trong luc hoan ma HP &lt; nguong_hp_thap thi roi xuong
     * buoc 4 "HP thap va HET THUOC" -> DUNG Tu dong (kazuhas4 4 lan lyDo=12 khi con 900 binh 94, guc 13 / 144 giay sau).
     * Nay: lenh dang bay (chua toi may chu) chi chan {@link #KHOANG_KE_THUOC_MS}; binh HP cach binh HP THAT truoc
     * (Char.timeUsePotion) {@link #KHOANG_THUOC_MS} va cach binh bat ky {@link #KHOANG_GIUA_BINH_MS}; binh PP cach binh bat
     * ky {@link #KHOANG_THUOC_MS} va nhuong binh HP khi HP duoi nguong. Te nhat 5 giay: PP, HP +0,8, +2,3, +3,8, +5,3 = 4
     * binh / 5 giay (tu_dong.nhip_thuoc 4/5s, du 300 ms tre mang).
     */
    public static final long KHOANG_KE_THUOC_MS = 700L;
    public static final long KHOANG_GIUA_BINH_MS = 800L;
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
        /** Quai dang nham nguoi choi nay (Monster.target) - "dung an toan" chi danh nhung con nay. */
        public boolean danhMinh;
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
        /** Bo nao vua ra lenh thuoc (ms truoc) - lenh dang bay chua toi may chu, khong ra lenh lap. */
        public long tuLanThuocMs = Long.MAX_VALUE;
        /** Binh HP / binh bat ky (HP hoac PP) DUNG THAT cuoi cung (ms truoc, Char.timeUsePotion) - MAX = chua. */
        public long tuLanBinhHpMs = Long.MAX_VALUE, tuLanBinhMs = Long.MAX_VALUE;
        /** Don sat thuong cuoi nhan vao (ms truoc, Char.soSatThuong) - MAX = chua bi danh. */
        public long tuLanBiDanhMs = Long.MAX_VALUE;
        public DiDuoc diDuoc;
        /**
         * 0.9.24: so chieu danh nguoi choi da gan, theo thu tu (client gui / trang chieu trong ho so). null hoac rong =
         * khong biet / chua gan chieu danh nao -> moi chieu trong {@link #chieu} (nhu truoc).
         */
        public int[] chieuGan;
        /** chieuGan la "Thu tu chieu" nguoi choi tu dat -> dung dung thu tu. */
        public boolean thuTuNghiem;
        /** Do cam tay hong: may chu tu choi moi don -> dung + bao (khong dung im danh khong ra don). */
        public boolean vuKhiHong;
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
        // 1. tui day / 1b. vu khi hong (31/10/2026: don van ra, 30% sat thuong): DUNG - nhung 12/11/2026 "dung an toan":
        // dang bi danh thi CHUA dung (truoc day dung ngay giua tran -> nguoi treo dung im giua bay quai, khong ai uong thuoc
        // -> guc: rongngoo / ruby guc 40-250 giay sau lyDo=15). Van uong thuoc, chi danh con dang danh minh, het bi danh
        // (SoSatThuong.GIAO_CHIEN_MS) moi dung.
        int dungCho = -1;
        if (c.dungKhiDayTui && t.tuiDay) {
            dungCho = TuDong.TAT_DAY_TUI;
        } else if (t.vuKhiHong) {
            dungCho = TuDong.TAT_VU_KHI_HONG;
        }
        if (dungCho >= 0 && !dangGiaoChien(t)) {
            k.hd = HD_DUNG;
            k.lyDoTat = dungCho;
            k.ly = TuDong.lyDo(dungCho);
            return k;
        }
        final int pt = phanTram(t.hp, t.maxhp);
        boolean hetBinhPP = false;
        final int nguongHp = nguong(t.nguongHpNguoi, c.nguongThuocHp);
        // con binh HP va duoc uong (nguoi choi / may chu khong tat thuoc HP): HP thap KHONG phai "het thuoc"
        final boolean coTheUongHp = c.tuDungThuoc && nguongHp > 0 && conBinh(t, BINH_HP);
        final boolean canHp = coTheUongHp && pt < nguongHp;
        // 3. thuoc (uu tien truoc PK: dang mat mau thi uong truoc da)
        if (c.tuDungThuoc && t.tuLanThuocMs >= KHOANG_KE_THUOC_MS) {
            if (canHp && t.tuLanBinhHpMs >= KHOANG_THUOC_MS && t.tuLanBinhMs >= KHOANG_GIUA_BINH_MS) {
                final int loai = chonBinh(t, BINH_HP, t.maxhp - t.hp);
                if (loai >= 0) {
                    k.hd = HD_THUOC;
                    k.so = loai;
                    k.ly = "uống thuốc HP (" + pt + "%)";
                    k.hoiPhuc = false;
                    return k;
                }
            }
            final int ptMp = phanTram(t.mp, t.maxmp);
            final int nguongMp = nguong(t.nguongMpNguoi, c.nguongThuocMp);
            // 0.9.22 (chu game: "het PP khong tu su dung khi dang dung auto"): PP khong du cho chieu ton PP re nhat thi
            // uong ca khi nguoi choi de nguong PP = Tat (0) - truoc do Auto chi danh chieu 0 PP mai, 678 binh PP nam yen.
            // May chu tat thuoc PP (tu_dong.nguong_thuoc_mp=0) thi van khong uong.
            final boolean hetPP = c.nguongThuocMp > 0 && t.maxmp > 0 && t.mp < ppToiThieu(t);
            // binh PP nhuong binh HP: HP duoi nguong (con binh HP) thi de nhip thuoc cho binh HP sap toi
            if (t.maxmp > 0 && (nguongMp > 0 && ptMp < nguongMp || hetPP) && !canHp && t.tuLanBinhMs >= KHOANG_THUOC_MS) {
                final int loai = chonBinh(t, BINH_MP, t.maxmp - t.mp);
                if (loai >= 0) {
                    k.hd = HD_THUOC;
                    k.so = loai;
                    k.ly = "uống thuốc PP (" + ptMp + "%)";
                    k.hoiPhuc = t.dangHoiPhuc;
                    return k;
                }
                hetBinhPP = !conBinh(t, BINH_MP);
            }
        }
        // 4. HP thap, HET THUOC that (khong con binh HP / nguoi choi tat thuoc HP): rut lui / dung. Con binh nhung dang
        // trong nhip thuoc -> danh tiep, buoc sau uong (12/11/2026: truoc day dung Tu dong voi ly do "het thuoc" sai).
        final boolean thap = c.nguongHpThap > 0 && pt < c.nguongHpThap && !coTheUongHp;
        boolean hoiPhuc = !coTheUongHp && t.dangHoiPhuc && t.veHoiPhucNguoi && pt < Math.max(c.nguongHpThap, c.hoiPhucToi);
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
        // 4b. dung an toan (tui day / vu khi hong giua tran): chi danh con dang danh minh, khong nhat do, khong tim con moi
        if (dungCho >= 0) {
            final Quai q = conDanhMinh(c, t);
            final String ly = "sắp dừng (" + (dungCho == TuDong.TAT_DAY_TUI ? "hành trang đầy" : "đồ cầm tay hỏng")
                    + ") - đánh nốt Pokémon đang tấn công";
            if (q == null) {
                k.hd = HD_CHO;
                k.ly = ly;
                return k;
            }
            danh(c, t, q, k, hetBinhPP);
            k.ly = ly;
            return k;
        }
        // 4c. ra ngoai vung (ban kinh + le): ve diem bat dau - truoc day may chu DUNG han (lyDo=5) giua tran khi duoi theo
        // con quai chay ra mep vung; nay chi dung khi ra xa han (TuDong.raVungXa).
        final long ngoai = (long) t.banKinh + TuDong.BAN_KINH_LE;
        if (khoang2(t.x, t.y, t.neoX, t.neoY) > ngoai * ngoai) {
            veCho(t, k, "quay lại vùng luyện tập", "quay lại vùng luyện tập");
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
        danh(c, t, q, k, hetBinhPP);
        return k;
    }

    /** Ra lenh danh {@code q} (buoc 6-7: muc tieu + chieu). */
    static void danh(final CauHinh c, final TinhHinh t, final Quai q, final KeHoach k, final boolean hetBinhPP) {
        k.hd = HD_DANH;
        k.id = q.id;
        k.x = q.x;
        k.y = q.y;
        // 7. chieu
        final Chieu ch = chonChieu(c, t, q);
        if (ch == null) {
            k.so = -1;
            k.ly = hetBinhPP ? "hết bình PP - chờ hồi PP" : "chờ hồi chiêu / PP";
            return;
        }
        k.so = ch.so;
        k.heSo = heSoChieu(c, t, ch, q);
        k.ly = k.heSo >= 150 ? "đánh - chiêu rất hiệu quả" : (k.heSo < 100 ? "đánh - chiêu kém hiệu quả" : "đang đánh");
        if (hetBinhPP) {
            k.ly = "hết bình PP - " + k.ly;
        }
    }

    /** Dang giao chien: vua bi danh (SoSatThuong.GIAO_CHIEN_MS) hoac co quai dang nham minh. */
    static boolean dangGiaoChien(final TinhHinh t) {
        if (t.tuLanBiDanhMs < SoSatThuong.GIAO_CHIEN_MS) {
            return true;
        }
        for (final Quai q : t.quai) {
            if (q != null && q.danhMinh && q.hp > 0 && !q.cuaNguoiKhac) {
                return true;
            }
        }
        return false;
    }

    /** Con quai gan nhat dang danh minh (bo boss khi may chu khong cho danh boss), null = khong. */
    static Quai conDanhMinh(final CauHinh c, final TinhHinh t) {
        Quai tot = null;
        long gan = Long.MAX_VALUE;
        for (final Quai q : t.quai) {
            if (q == null || !q.danhMinh || q.hp <= 0 || q.cuaNguoiKhac || q.boss && !c.danhBoss) {
                continue;
            }
            final long d = khoang2(t.x, t.y, q.x, q.y);
            if (d < gan) {
                gan = d;
                tot = q;
            }
        }
        return tot;
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

    /** Con it nhat mot binh trong bang (ke ca dang hoi). */
    static boolean conBinh(final TinhHinh t, final int[][] bang) {
        for (int i = 0; i < bang.length; i++) {
            final int loai = bang[i][0];
            if (loai < t.thuoc.length && t.thuoc[loai] > 0) {
                return true;
            }
        }
        return false;
    }

    /** PP cua chieu danh (se dung) ton PP re nhat (bo chieu 0 PP); 0 = khong co chieu nao ton PP. */
    static int ppToiThieu(final TinhHinh t) {
        int min = 0;
        for (final Chieu ch : chieuDung(t)) {
            if (ch.pp > 0 && (min == 0 || ch.pp < min)) {
                min = ch.pp;
            }
        }
        return min;
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
        final int heSo = KhacHePve.ra(heSoChieu(c, t, ch, q)); // sat thuong that vao quai: he so da kep PvE (60..150)
        final int stab = c.chonChieuTheoHe && ch.he >= 0 ? ChieuPokemon.heSoStab(ch.he, t.he1, t.he2) : 100;
        return (long) Math.max(1, ch.satThuong) * heSo * stab;
    }

    /**
     * Chieu se dung theo thu tu: chieu da gan ({@link TinhHinh#chieuGan}) co trong {@link TinhHinh#chieu} (da hoc, khong
     * buff), khong trung; khong gan / khong chieu gan nao con hoc = moi chieu da hoc (chinh {@code t.chieu}).
     */
    public static List<Chieu> chieuDung(final TinhHinh t) {
        if (t.chieuGan == null || t.chieuGan.length == 0) {
            return t.chieu;
        }
        final List<Chieu> ds = new ArrayList<Chieu>();
        for (final int so : t.chieuGan) {
            for (final Chieu ch : t.chieu) {
                if (ch.so == so && !ds.contains(ch)) {
                    ds.add(ch);
                    break;
                }
            }
        }
        return ds.isEmpty() ? t.chieu : ds;
    }

    /** He so trong game cua don mien nhiem (HePokemon.heSoTrongGame: chinh xac 0 -> 30). */
    static final int HE_SO_MIEN_NHIEM = 30;

    static boolean dungDuoc(final TinhHinh t, final Chieu ch) {
        return ch.conHoi <= 0L && ch.pp <= t.mp;
    }

    /**
     * Chieu danh buoc nay, CHI trong chieu nguoi choi da gan ({@link #chieuDung}): het hoi, du PP. "Thu tu chieu" tu dat:
     * chieu dau tien dung duoc (bo chieu mien nhiem khi con chieu khac). Trang chieu / khong gan: bang diem, uu tien chieu
     * it PP hon (giu PP cho chieu manh), roi o truoc. Khong chieu da gan nao dung duoc: danh thuong (chieu 0) neu san.
     */
    static Chieu chonChieu(final CauHinh c, final TinhHinh t, final Quai q) {
        final List<Chieu> ds = chieuDung(t);
        final boolean gan = ds != t.chieu;
        Chieu tot = null;
        if (gan && t.thuTuNghiem) {
            for (final Chieu ch : ds) {
                if (!dungDuoc(t, ch)) {
                    continue;
                }
                if (heSoChieu(c, t, ch, q) > HE_SO_MIEN_NHIEM) {
                    return ch;
                }
                if (tot == null) {
                    tot = ch; // mien nhiem: chi dung khi khong con chieu nao khac
                }
            }
        } else {
            long diemTot = -1L;
            for (final Chieu ch : ds) {
                if (!dungDuoc(t, ch)) {
                    continue;
                }
                final long d = diem(c, t, ch, q);
                if (d > diemTot || d == diemTot && tot != null && ch.pp < tot.pp) {
                    diemTot = d;
                    tot = ch;
                }
            }
        }
        if (tot == null && gan) {
            for (final Chieu ch : t.chieu) {
                if (ch.so == 0 && dungDuoc(t, ch)) {
                    return ch; // danh thuong khi moi chieu da gan dang hoi / thieu PP
                }
            }
        }
        return tot;
    }

    /**
     * So chieu danh tren MOT trang o nhanh luu o ho so (NgoaiHinhGame: 5 o x 12 bit, (kieu+1) 3 bit | gia tri 8 bit | buff
     * 1 bit, o 0 o bit thap; kieu 1 = chieu). Bo o trong / thuoc / o danh dau bo tro, khong trung; theo thu tu o.
     */
    public static int[] chieuTrongTrang(final long trang) {
        final int[] tam = new int[5];
        int n = 0;
        for (int i = 0; i < 5; i++) {
            final int m = (int) ((trang >> (12 * i)) & 0xFFF);
            if (((m >> 9) & 7) != 2 || (m & 1) != 0) {
                continue;
            }
            final int so = (m >> 1) & 255;
            boolean trung = false;
            for (int k = 0; k < n; k++) {
                trung |= tam[k] == so;
            }
            if (!trung) {
                tam[n++] = so;
            }
        }
        return java.util.Arrays.copyOf(tam, n);
    }
}
