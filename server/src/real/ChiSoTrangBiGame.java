package real;

import com.htbb.pokemon.ChiSoTrangBi;
import com.htbb.pokemon.GioiHanChiSo;
import com.htbb.pokemon.HePokemon;
import com.htbb.pokemon.LauDai;
import com.htbb.pokemon.NganSachTrangBi;
import com.htbb.pokemon.TrangThaiPokemon;
import java.util.Vector;

/**
 * Noi GioiHanChiSo / NganSachTrangBi / ChiSoTrangBi (lop thuan) vao Char luc chay. CHI co tac dung khi bat
 * sv.lau_dai VA khoa cau hinh {@code chi_so.bat} (mac dinh 1). Tat co: moi ham tra ve dung gia tri cu.
 *
 * Moi gioi han dat o DIEM CUOI (cho roll chi mang, ne, hut HP, giam sat thuong...) chu khong o tung nguon,
 * nen pet / thu cuoi / skin / thoi trang / bao vat / do giu / buff deu di qua cung mot tran: khong nguon nao
 * "lach" duoc tran (docs/thiet-ke-game.md).
 */
public final class ChiSoTrangBiGame {

    private ChiSoTrangBiGame() {
    }

    // ------------------------------------------------------------------ cau hinh (cau_hinh, ap dung ngay)

    public static final String K_BAT = "chi_so.bat";
    public static final String K_KEP_TRANG_BI = "chi_so.kep_trang_bi";
    public static final String K_TRAN_CHI_MANG = "chi_so.tran_chi_mang";
    public static final String K_TRAN_ST_CHI_MANG = "chi_so.tran_st_chi_mang";
    public static final String K_TRAN_NE = "chi_so.tran_ne";
    public static final String K_TRAN_HUT = "chi_so.tran_hut";
    public static final String K_TRAN_HUT_PVP = "chi_so.tran_hut_pvp";
    public static final String K_TRAN_GIAM_ST = "chi_so.tran_giam_st";
    public static final String K_TRAN_KHANG_HE = "chi_so.tran_khang_he";
    public static final String K_SAN_SAT_THUONG = "chi_so.san_sat_thuong";
    public static final String K_TRAN_PHAN_TRAM_CONG = "chi_so.tran_phan_tram_cong";

    /** Gioi han chi so dang bat (sv.lau_dai=1 va chi_so.bat != 0). */
    public static boolean bat() {
        return LauDai.bat() && LauDai.so(K_BAT, 1) != 0;
    }

    /** Kep chi so trang bi theo ngan sach o / cap / nang cap (chi_so.kep_trang_bi, mac dinh 1). */
    public static boolean kepTrangBi() {
        return bat() && LauDai.so(K_KEP_TRANG_BI, 1) != 0;
    }

    /** Tran (phan nghin) doc de tu cau_hinh; goi giam dan co gian theo cung ti le. */
    static int giamDan(final long tho, final int goi, final int tranMacDinh, final String khoa) {
        final int tran = Math.max(1, Math.min(1000, LauDai.so(khoa, tranMacDinh)));
        final int goiMoi = (int) ((long) goi * tran / tranMacDinh);
        return GioiHanChiSo.giamDan(tho, goiMoi, tran);
    }

    // ------------------------------------------------------------------ bang gia tri theo id (cache moi lan mac/thao)

    /**
     * Tong gia tri thuoc tinh theo id (0..186) cua do dang mac + mat na + bao vat huyen thoai. Goi o dau
     * Char.calculateAttrib (moi lan mac/thao). Hai dong phien da (93/94) KHONG cong don: lay cua phien da.
     */
    public static void capNhat(final Char p) {
        if (p == null) {
            return;
        }
        if (!bat()) {
            p.ldGiaTri = null;
            return;
        }
        final int[] g = new int[187];
        try {
            final Vector<Item> ws = p.getCharWearing();
            for (final Item it : ws) {
                if (it == null || it.level > p.lvDetail.lv) {
                    continue;
                }
                cong(g, it, it.getTemplate() != null && it.getTemplate().type == 12);
            }
            BoNgocGame.capNhat(p, ws); // [bo-ngoc] dem bo cung bac + ngoc he dang khảm
            if (p.wModel != null && p.wModel.matNa != null) {
                cong(g, p.wModel.matNa, false);
            }
            if (p.itemThanKhi != null) {
                cong(g, p.itemThanKhi, false);
            }
        } catch (final Exception e) {
            // khong de loi du lieu mot mon lam hong ca tinh chi so
        }
        p.ldGiaTri = g;
    }

    private static void cong(final int[] g, final Item it, final boolean laPhienDa) {
        if (it.atb != null) {
            for (int i = 0; i < 33 && i < it.atb.length; i++) {
                g[i] += Math.max(0, it.atb[i]);
            }
        }
        if (it.newAtb != null) {
            for (int i = 0; i < 10 && i < it.newAtb.length; i++) {
                g[33 + i] += Math.max(0, it.newAtb[i]);
            }
        }
        if (it.otherAtt != null) {
            for (int i = 0; i < 126 && i < it.otherAtt.length; i++) {
                final int id = 61 + i;
                final int v = Math.max(0, it.otherAtt[i]);
                if (id == ChiSoTrangBi.ID_ST_HE_PHIEN_DA || id == ChiSoTrangBi.ID_KHANG_HE_PHIEN_DA) {
                    if (laPhienDa && v > 0) {
                        g[id] = v;
                    }
                } else {
                    g[id] += v;
                }
            }
        }
    }

    static int gt(final Char p, final int id) {
        final int[] g = p.ldGiaTri;
        return g != null && id >= 0 && id < g.length ? g[id] : 0;
    }

    // ------------------------------------------------------------------ kep chi so mon

    /** Chi so atb[idx] (0..6) cua mon {@code it} dang mac, sau khi kep ngan sach (tat co: tra nguyen). */
    public static int atb(final Char p, final Item it, final int idx) {
        final int v = it.atb[idx];
        if (v <= 0 || idx >= NganSachTrangBi.SO_CHI_SO || it.getTemplate() == null) {
            return v;
        }
        final ItemTemplates t = it.getTemplate();
        if (t.atb != null && t.atb.length > 9 && t.atb[9] != 0) {
            return v; // thoi trang: atb4..8 la so hieu hinh
        }
        int kq = v;
        if (kepTrangBi()) {
            final int capNv = p != null && p.lvDetail != null ? p.lvDetail.lv : t.level;
            kq = NganSachTrangBi.kep(t.type, idx, t.level, it.plus, capNv, v);
        }
        return congNgoc(it, kq);
    }

    /** [bo-ngoc] chi so chinh mon (da kep) + % ngoc he khảm tren mon (tran 10% / mon). Tat co: tra nguyen. */
    static int congNgoc(final Item it, final int v) {
        if (v <= 0 || !bat()) {
            return v;
        }
        final int pct = BoNgocGame.phanTramNgocMon(it);
        return pct <= 0 ? v : (int) Math.min(Short.MAX_VALUE, v + (long) v * pct / 100L);
    }

    /**
     * [bo-ngoc] Thuong bo tong (%): thuong bo nang cap cu (+10/+13/+15 du bo, {@code cuPhanTram}) + bo cung bac
     * (data_noi_dung_bo, BoNgocGame) - CHUNG mot tran 5% (GioiHanChiSo.thuongBo). Tat co: chi phan cu.
     */
    static int thuongBoTong(final Char p, final int chiSo, final int cuPhanTram) {
        if (!bat()) {
            return cuPhanTram;
        }
        return GioiHanChiSo.thuongBo(cuPhanTram + BoNgocGame.boPhanNghin(p, chiSo) / 10);
    }

    // ------------------------------------------------------------------ chi mang

    /** Phan nghin chi mang THO (chua giam dan): diem /300 (+ bang hoi, do giu, buff) + bao kich. */
    static long chiMangTho(final Char p) {
        final int diem = p.critical + p.getEffSkillClan(1) * p.critical / 100 + VatPhamDungLauDai.congChiMang(p);
        // [bo-ngoc] + chi mang bo 11 mon cung bac (phan nghin, <= 3%) - van qua tran chi mang 40% o diem quay
        return GioiHanChiSo.chiMangTho(diem, p.baokich) + BoNgocGame.boPhanNghin(p, com.htbb.pokemon.BoTrangBi.CHI_MANG);
    }

    /** Thay Char.havecrit khi bat: mot lan quay cho ca chi mang + bao kich, tran 40% giam dan. */
    static boolean quayChiMang(final Char p) {
        return Map.random(1000) < giamDan(chiMangTho(p), GioiHanChiSo.CHI_MANG_GOI, GioiHanChiSo.CHI_MANG_TRAN,
                K_TRAN_CHI_MANG);
    }

    /**
     * PvP: don da quay chi mang PvE thi giu lai voi ti le nay -> ti le that = tran PvP 30% (giam dan), tru
     * "Khang chi mang" (mat na, id 76) cua muc tieu. Tat co: luon giu.
     */
    public static boolean giuChiMangPvp(final Char p, final LiveActor muc) {
        if (!bat() || p == null) {
            return true;
        }
        long tho = chiMangTho(p);
        if (muc instanceof Char) {
            tho = Math.max(0L, tho - ((Char) muc).getKhangBaoKichMatNa());
        }
        final int pve = giamDan(chiMangTho(p), GioiHanChiSo.CHI_MANG_GOI, GioiHanChiSo.CHI_MANG_TRAN, K_TRAN_CHI_MANG);
        if (pve <= 0) {
            return false;
        }
        final int pvp = GioiHanChiSo.chiMang(tho, true);
        return Map.random(1000) < (int) Math.min(1000L, pvp * 1000L / pve);
    }

    /** He so don chi mang PvE (phan tram): 150 + ST chi mang them (bao vat + dong 41 + lien kich pet), tran 200. */
    static int heSoChiMang(final Char p) {
        long them = p.getTkStChiMang();
        them += Math.max(0, (int) p.newAtb[8]) * 10L;
        if (p.petUsing != null) {
            them += Math.max(0, p.petUsing.lienKichGoc()) * 10L;
        }
        return com.htbb.pokemon.SatThuongLauDai.CHI_MANG_PHAN_TRAM
                + giamDan(them, GioiHanChiSo.ST_CHI_MANG_GOI, GioiHanChiSo.ST_CHI_MANG_TRAN, K_TRAN_ST_CHI_MANG) / 10;
    }

    // ------------------------------------------------------------------ ne tranh

    /** Ne tranh phan nghin cua muc tieu sau tran (PvE 30%, PvP 20%). */
    static int neTranh(final int phanNghin, final boolean pvp) {
        if (pvp) {
            return GioiHanChiSo.neTranh(phanNghin, true);
        }
        return giamDan(phanNghin, GioiHanChiSo.NE_GOI, GioiHanChiSo.NE_TRAN, K_TRAN_NE);
    }

    // ------------------------------------------------------------------ hut HP (gop moi nguon)

    /**
     * Phan nghin hut HP theo sat thuong gay ra: dong 77 (truoc la HP phang 20..55 -> nay phan nghin), dong 129
     * (mat na, %), 182 (bao vat, %), do giu "hut" (%), thoi trang hut mau (2%), pet doi (ti le x luong ~ %).
     */
    static int hutPhanNghin(final Char p, final boolean pvp) {
        long t = Math.max(0, (int) p.hutmau);
        t += Math.max(0, p.getPcChuyenStThanhHpMatNa()) * 10L;
        t += Math.max(0, p.getTkHutHp()) * 10L;
        t += Math.max(0, VatPhamDungLauDai.doGiuSo(p, "hut")) * 10L;
        if (p.wModel != null && p.wModel.haveHutMau()) {
            t += 20L;
        }
        if (p.petUsing != null && p.petUsing.getpcHutHpByHit() > 0) {
            t += Math.min(30, p.petUsing.getpcHutHpByHit() / 10);
        }
        if (pvp) {
            return GioiHanChiSo.giamDan(t, GioiHanChiSo.HUT_PVP_GOI,
                    Math.max(1, Math.min(1000, LauDai.so(K_TRAN_HUT_PVP, GioiHanChiSo.HUT_PVP_TRAN))));
        }
        return giamDan(t, GioiHanChiSo.HUT_GOI, GioiHanChiSo.HUT_TRAN, K_TRAN_HUT);
    }

    /**
     * Hoi HP cho {@code p} sau don {@code dam} (PvP: tinh tren sat thuong that x0,30; thu thach - boss / Gym /
     * tinh anh - con 10%; moi don &lt;= 0,2% HP toi da).
     */
    static void hutSauDon(final Char p, final int dam, final boolean pvp, final boolean thuThach) {
        if (dam <= 0 || p.hp <= 0 || p.hp >= p.maxhp) {
            return;
        }
        final long that = pvp ? GioiHanChiSo.satThuongThatPvp(dam) : dam;
        final int hoi = (int) GioiHanChiSo.hutMotDon(that, hutPhanNghin(p, pvp), thuThach, p.maxhp);
        if (hoi > 0) {
            p.hp = Math.min(p.maxhp, p.hp + hoi);
        }
        final int pcMp = Math.max(0, p.getTkHutMp());
        if (pcMp > 0 && p.mp < p.maxmp) {
            final int hoiMp = (int) GioiHanChiSo.hutMotDon(that, GioiHanChiSo.hut(pcMp * 10L, pvp), thuThach, p.maxmp);
            if (hoiMp > 0) {
                p.mp = Math.min(p.maxmp, p.mp + hoiMp);
            }
        }
    }

    /**
     * Ban co tran cua CharChiSo.applyThanKhiOnHit (goi khi {@link #bat()}): cong ST bao vat (theo % HP, boss,
     * quai), khang he + giam ST nguoi choi cua muc tieu, hut HP/PP gop, cam chieu, lam yeu, phan don.
     */
    static int trenDon(final Char self, final LiveActor actor, final int damVao) {
        int dam = damVao;
        try {
            final boolean laNguoi = actor.cat == 0 && actor instanceof Char;
            final boolean laBoss = actor.isBoss() || actor.isTruMoba();
            if (self.itemThanKhi != null) {
                final int tkHp = self.getTkStTheoHp();
                if (tkHp > 0 && actor.maxhp > 0 && !actor.isBossTruRong()) {
                    dam += stTheoHp(actor.maxhp, tkHp, laBoss, dam);
                }
                final int tkMp = self.getTkStTheoMp();
                if (tkMp > 0 && laNguoi && ((Char) actor).maxmp > 0) {
                    dam += stTheoHp(((Char) actor).maxmp, tkMp, false, dam);
                }
                if (laBoss) {
                    dam += (int) ((long) dam * GioiHanChiSo.tran(self.getTkStBoss(), GioiHanChiSo.ST_BOSS_TRAN) / 1000L);
                } else if (actor.cat == 1) {
                    dam += (int) ((long) dam * GioiHanChiSo.tran(self.getTkStQuai(), GioiHanChiSo.ST_QUAI_TRAN) / 1000L);
                }
            }
            if (laNguoi) {
                final Char bidanh = (Char) actor;
                batDauDon(bidanh);
                // khang he theo he CHIEU dang danh (thay khang theo dong cu), tran 60%
                dam = giam(bidanh, dam, khangHe(bidanh, self.lauDaiHe));
                dam = khuechDaiHe(bidanh, dam, self.lauDaiHe); // [bo-ngoc] ngoc tru khang qua muc
                // giam ST tu nguoi choi (bao vat) + khang cua thu cuoi: mot lop PvP tran 40%
                final long g = Math.max(0, bidanh.getTkGiamStPlayer()) + Math.max(0, bidanh.getKhangNguHanhThuCuoi(self.charClass));
                dam = giam(bidanh, dam, tranGiam(g, true));
            }
            hutSauDon(self, dam, laNguoi, laBoss || (actor instanceof Monster && NoiDungLauDai.laAlpha((Monster) actor)));
            if (self.itemThanKhi != null) {
                final int pcCamLang = Math.min(laNguoi ? 3 : 5, self.getTkCamLang());
                if (pcCamLang > 0 && Map.r.nextInt(100) < pcCamLang) {
                    actor.addEffBuff(EffectBuff.LAM_THINH, System.currentTimeMillis() + 3000L, EffectBuff.BY_ACTOR, 0);
                }
                final int tkSuyYeu = self.getTkGiamSucManhDich();
                if (tkSuyYeu > 0) {
                    actor.addEffBuff(EffectBuff.SUY_YEU, System.currentTimeMillis() + Char.tkSuyYeuThoiGian(tkSuyYeu),
                            EffectBuff.BY_ACTOR, Math.min(20, tkSuyYeu / 10));
                }
            }
            if (laNguoi) {
                final Char bidanh = (Char) actor;
                if (bidanh.getPetUsing() != null && dam > 0) {
                    dam = bidanh.getPetUsing().applyKhangXuyenGiap(dam);
                }
                if (bidanh.getAnimalRide() != null && dam > 0) {
                    dam = bidanh.getAnimalRide().applyKhangXuyenGiap(dam);
                }
                final int pcPhan = phanDonPhanTram(bidanh.getTkPhanDon(), true);
                if (pcPhan > 0 && dam > 0) {
                    int damPhan = self.giamSatThuongPhanDon((int) ((long) GioiHanChiSo.satThuongThatPvp(dam) * pcPhan / 100L));
                    if (damPhan > 0) {
                        self.hp = Math.max(0, self.hp - Math.min(damPhan, self.hp));
                    }
                }
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }
        return Math.max(0, dam);
    }

    // ------------------------------------------------------------------ san sat thuong (chong bat tu)

    /** Bat dau mot don moi danh vao {@code c}: dat lai luong con lai = 100%. */
    public static void batDauDon(final LiveActor c) {
        if (c instanceof Char) {
            final Char x = (Char) c;
            x.ldConLai = 1000;
            x.ldConLaiMs = System.currentTimeMillis();
        }
    }

    /**
     * Mot lop giam {@code phanNghin} tren don {@code dam} danh vao {@code c}, ton trong SAN: sau moi lop don
     * van con &gt;= 25% (chi_so.san_sat_thuong) so voi luc bat dau. Tat co: tru thang nhu cu.
     */
    public static int giam(final LiveActor c, final int dam, final int phanNghin) {
        if (dam <= 0 || phanNghin <= 0) {
            return dam;
        }
        if (!bat() || !(c instanceof Char)) {
            return (int) Math.max(0L, dam - (long) dam * Math.min(1000, phanNghin) / 1000L);
        }
        final Char x = (Char) c;
        final long bay = System.currentTimeMillis();
        if (bay - x.ldConLaiMs > 40L) {
            x.ldConLai = 1000; // don moi (duong khong goi batDauDon: boss, quai dac biet)
        }
        x.ldConLaiMs = bay;
        final int san = Math.max(0, Math.min(1000, LauDai.so(K_SAN_SAT_THUONG, GioiHanChiSo.SAN_SAT_THUONG)));
        final int duoc;
        if (x.ldConLai <= san) {
            duoc = 0;
        } else {
            final int toiDa = 1000 - (int) ((long) san * 1000L / x.ldConLai);
            duoc = Math.max(0, Math.min(Math.min(1000, phanNghin), toiDa));
        }
        x.ldConLai = (int) ((long) x.ldConLai * (1000 - duoc) / 1000L);
        return (int) Math.max(dam > 0 ? 1L : 0L, dam - (long) dam * duoc / 1000L);
    }

    /** Lop giam bang luong tuyet doi {@code bot} (cach cu "finalDam -= a"): doi ra phan nghin roi qua {@link #giam}. */
    public static int giamLuong(final LiveActor c, final int dam, final long bot) {
        if (dam <= 0 || bot <= 0L) {
            return dam;
        }
        return giam(c, dam, (int) Math.min(1000L, bot * 1000L / dam));
    }

    /** Giam sat thuong thu dong tong (phan nghin) sau tran: PvE 50%, PvP 40%. */
    static int tranGiam(final long tho, final boolean pvp) {
        if (pvp) {
            return GioiHanChiSo.giamSatThuong(tho, true);
        }
        return giamDan(tho, GioiHanChiSo.GIAM_GOI, GioiHanChiSo.GIAM_TRAN, K_TRAN_GIAM_ST);
    }

    // ------------------------------------------------------------------ he

    /** Khang he {@code he} cua {@code c} (phan nghin, tran 60%). he &lt; 0 -> chi "khang moi he". */
    public static int khangHe(final Char c, final int he) {
        if (c.ldGiaTri == null) {
            return 0;
        }
        long tho = he >= 0 ? ChiSoTrangBi.khangHeTho(c.ldGiaTri, he) : gt(c, ChiSoTrangBi.ID_KHANG_MOI_HE);
        if (he >= 0) {
            tho -= BoNgocGame.khangTruPhanNghin(c, he); // [bo-ngoc] phan "hai" cua ngoc he (Ngoc Lua -> -khang Nuoc)
        }
        return giamDan(Math.max(0L, tho), GioiHanChiSo.KHANG_HE_GOI, GioiHanChiSo.KHANG_HE_TRAN, K_TRAN_KHANG_HE);
    }

    /** Tran phan khuech dai khi tru khang cua ngoc vuot khang dang co (phan nghin). */
    public static final int TRAN_KHUECH_DAI_HE = 150;

    /**
     * [bo-ngoc] Phan tru khang cua ngoc vuot qua khang he dang co -&gt; don cua he do manh hon (toi da +15%). Goi SAU lop
     * khang he (cung noi goi khangHe). Tat co / khong co ngoc: tra nguyen.
     */
    public static int khuechDaiHe(final Char c, final int dam, final int he) {
        if (dam <= 0 || he < 0 || c == null || c.ldGiaTri == null || !bat()) {
            return dam;
        }
        final long du = BoNgocGame.khangTruPhanNghin(c, he) - ChiSoTrangBi.khangHeTho(c.ldGiaTri, he);
        if (du <= 0) {
            return dam;
        }
        return (int) Math.min(Integer.MAX_VALUE, dam + (long) dam * Math.min(TRAN_KHUECH_DAI_HE, du) / 1000L);
    }

    /** % do he (ST he 163..167 + phien da), kep THIET_KE 3.4 = 20%. Truyen vao SatThuongLauDai.tinh. */
    public static int doHePhanTram(final Char p, final int he) {
        if (!bat() || p == null || p.ldGiaTri == null || he < 0 || he >= HePokemon.SO_HE) {
            return 0;
        }
        // [bo-ngoc] + ST he cua ngoc he khảm (n x 1,5 phan nghin) - cung tran 20%
        return (int) Math.min(ChiSoTrangBi.TRAN_DO_HE_PHAN_TRAM,
                (ChiSoTrangBi.stHeTho(p.ldGiaTri, he) + BoNgocGame.stHePhanNghin(p, he)) / 10L);
    }

    // ------------------------------------------------------------------ trang thai tu do khảm (thay buffSkillKham)

    /**
     * Gay trang thai Pokemon tu dong 14..18 (khảm cu) khi bat: ti le moi trang thai giam dan tran 15% (PvP 7,5%),
     * tru khang 21..25 cua muc tieu (tran 50%), thoi luong / mien theo TrangThaiPokemon (PvP x0,5, boss 30%).
     */
    static void khamTrangThai(final Char p, final LiveActor muc) {
        if (muc == null || muc.hp <= 0) {
            return;
        }
        final boolean pvp = muc instanceof Char;
        final int[] gay = {p.khamMu(), p.khamDoc(), p.khamChoang(), p.khamBang(), p.khamHoathach()};
        final int[] khang = {muc.khamKhangMu(), muc.khamKhangDoc(), muc.khamKhangChoang(), muc.khamKhangBang(),
            muc.khamKhangHoathach()};
        for (int i = 0; i < gay.length; i++) {
            if (gay[i] <= 0) {
                continue;
            }
            final int tl = GioiHanChiSo.gayTrangThai(gay[i], pvp);
            final int kh = GioiHanChiSo.khangTrangThai(Math.max(0, khang[i]));
            final int thuc = tl * (1000 - kh) / 1000;
            if (thuc > 0 && Map.random(1000) < thuc) {
                if (TrangThaiGame.gayTuTrangBi(p, muc, ChiSoTrangBi.TRANG_THAI_THEO_KHAM[i]) != 0) {
                    return; // moi don toi da 1 trang thai
                }
            }
        }
    }

    // ------------------------------------------------------------------ hien thi

    /**
     * Loc danh sach dong hien tren tooltip khi bat: bo dong ngung / noi bo, kep atb0..6 theo ngan sach (de
     * so hien = so dung tinh), doi 2 dong phien da sang phan nghin. Tat co: tra nguyen.
     */
    public static Vector<InfoItemAttribute> locHienThi(final Item it, final Vector<InfoItemAttribute> ds, final Char p) {
        if (ds == null || !bat()) {
            return ds;
        }
        final ItemTemplates t = it.getTemplate();
        final boolean thoiTrang = t == null || (t.atb != null && t.atb.length > 9 && t.atb[9] != 0);
        final Vector<InfoItemAttribute> ra = new Vector<InfoItemAttribute>(ds.size());
        for (final InfoItemAttribute a : ds) {
            if (a == null) {
                continue;
            }
            final int id = a.id & 0xFF;
            if (ChiSoTrangBi.an(id)) {
                continue;
            }
            if (id == ChiSoTrangBi.ID_ST_HE_PHIEN_DA || id == ChiSoTrangBi.ID_KHANG_HE_PHIEN_DA) {
                ra.add(new InfoItemAttribute((short) id, ChiSoTrangBi.phanNghinDongPhienDa(a.value)));
                continue;
            }
            if (!thoiTrang && id < NganSachTrangBi.SO_CHI_SO && kepTrangBi()) {
                final int capNv = p != null && p.lvDetail != null ? p.lvDetail.lv : t.level;
                final int v = NganSachTrangBi.kep(t.type, id, t.level, it.plus, capNv, a.value);
                if (v != a.value) {
                    ra.add(new InfoItemAttribute((short) id, v));
                    continue;
                }
            }
            ra.add(a);
        }
        return ra;
    }

    /** Ten he da chon tren phien da (dong 93/94), "" neu chua chon - noi vao ten mon khi bat. */
    public static String tenHePhienDa(final Item it) {
        if (!bat() || it == null || it.otherAtt == null || it.otherAtt.length <= 33) {
            return "";
        }
        int he = ChiSoTrangBi.heCuaDongPhienDa(it.otherAtt[ChiSoTrangBi.ID_ST_HE_PHIEN_DA - 61]);
        if (he < 0) {
            he = ChiSoTrangBi.heCuaDongPhienDa(it.otherAtt[ChiSoTrangBi.ID_KHANG_HE_PHIEN_DA - 61]);
        }
        return he < 0 ? "" : " (hệ " + HePokemon.TEN[he] + ")";
    }

    // ------------------------------------------------------------------ hoi phuc, toc, trang thai khac

    /** Luong hoi moi nhip (5 giay) sau tran 2% toi da. */
    static int hoiNhip(final int tho, final int toiDa) {
        return bat() ? GioiHanChiSo.hoiNhip(tho, toiDa) : tho;
    }

    /** Ti le gay cham / cam chieu tu trang bi (phan nghin, tran 5%). */
    static int tiLeKhongChe(final int phanNghin) {
        return bat() ? Math.min(phanNghin, 50) : phanNghin;
    }

    /** Toc chay cong them cua bao vat (+1) chi khi chua vuot +30% toc co ban. */
    static int tocChayThem(final int tocHienTai, final int them) {
        if (!bat() || them <= 0) {
            return them;
        }
        final int toiDa = GioiHanChiSo.TOC_CHAY_CO_BAN + GioiHanChiSo.tocChayThemToiDa(GioiHanChiSo.TOC_CHAY_CO_BAN);
        return Math.max(0, Math.min(them, Math.max(toiDa, GioiHanChiSo.TOC_CHAY_CO_BAN + 1) - tocHienTai));
    }

    /** % giam hoi chieu (toc danh) sau tran 33% (= +50% toc danh). Tat co: tran cu 50%. */
    static int giamHoiChieu(final int phanTram) {
        return bat() ? GioiHanChiSo.giamHoiChieuPhanTram(phanTram) : Math.min(50, phanTram);
    }

    /** Phan don % sau tran (PvE 15%, PvP 10%). */
    static int phanDonPhanTram(final int phanTram, final boolean pvp) {
        return bat() ? GioiHanChiSo.phanDon(phanTram * 10L, pvp) / 10 : phanTram;
    }

    /** Xuyen giap (ti le bo qua thu, %) sau tran 30%. */
    static int xuyenGiapPhanTram(final int phanTram) {
        return bat() ? GioiHanChiSo.xuyenGiap(phanTram * 10L) / 10 : phanTram;
    }

    /** Thuong bo % (legacy +1/+4/+8/+12) sau tran 5%. */
    static int thuongBo(final int phanTram) {
        return bat() ? GioiHanChiSo.thuongBo(phanTram) : phanTram;
    }

    /** Kep tong % cong them: {@code moi} &lt;= goc x (1 + 50%). Tat co: tra nguyen. */
    static int kepPhanTram(final long moi, final long goc) {
        if (!bat()) {
            return (int) Math.max(Integer.MIN_VALUE, Math.min(Integer.MAX_VALUE, moi));
        }
        final int pt = Math.max(0, Math.min(5000, LauDai.so(K_TRAN_PHAN_TRAM_CONG, GioiHanChiSo.PHAN_TRAM_CONG_TRAN)));
        if (goc <= 0L) {
            return (int) Math.max(0L, Math.min(Integer.MAX_VALUE, moi));
        }
        return (int) Math.max(0L, Math.min(Integer.MAX_VALUE, Math.min(moi, goc * (1000L + pt) / 1000L)));
    }

    /** % EXP tu trang bi / pet / thoi trang / do giu sau tran 30%. */
    static int expTrangBi(final int phanTram) {
        return bat() ? GioiHanChiSo.expTrangBi(phanTram) : phanTram;
    }

    /** Sat thuong theo % HP muc tieu sau tran (thuong 1%, boss 0,2%, &lt;= don goc). */
    static int stTheoHp(final long hpToiDa, final int phanNghin, final boolean boss, final int donGoc) {
        return (int) GioiHanChiSo.stTheoHp(hpToiDa, phanNghin, boss, donGoc);
    }

    static boolean laTrangThai(final int tt) {
        return tt > TrangThaiPokemon.KHONG && tt <= TrangThaiPokemon.BANG;
    }
}
