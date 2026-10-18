package real;

import com.htbb.pokemon.CapDoLauDai;
import com.htbb.pokemon.CuonHut;
import com.htbb.pokemon.HoSoLauDai;
import com.htbb.pokemon.LauDai;
import data.Database;
import data.DatabaseLauDai;
import java.util.ArrayList;
import java.util.List;

/**
 * HAM DUY NHAT cong EXP ha quai khi {@link LauDai#bat()} (GD2 buoc 3.4). Thay 7 ban sao
 * song song (Region, RegionVantienTran, MapTown, MapLienDau, MapDanhNhau, MapClan, CharCopy,
 * CharTrangBi): moi ban sao re nhanh "if (LauDai.bat()) { ExpLauDai.traoHaQuai(...) } else
 * { ...code cu... }" ngay tai cho goi Map.calculatorXpParty.
 *
 * <pre>
 * exp = raw x heSoLechCap(cap nguoi - cap quai) x (1 + buff%, tran +100%) x nang luong%
 *           x bat kip% x to doi% (100 + 10 x (so nguoi gan - 1), toi da +40%)
 * BO: x3 TangEXP, giu 10%/50% theo cap, chia 3 tu cap 60, x0,6/0,65 tu cap 75, ve x2,5/x3,
 *     ngay x50, Map.doubleALL.
 * </pre>
 * Moi thanh vien o gan (cung map/khu/vung, 320px, con song) deu nhan PHAN DAY DU (khong chia).
 */
public final class ExpLauDai {

    /** Ban kinh "o gan" khi chia EXP to doi - giong code cu. */
    static final int GAN = 320;

    private ExpLauDai() {
    }

    /**
     * Ha quai: trao EXP cho {@code p} va dong doi o gan. {@code raw} = EXP goc (cung so code cu
     * dua vao calculatorXpParty, TRUOC giam lech cap cu - xem cho goi).
     */
    public static void traoHaQuai(final Char p, final LiveActor mt, final long raw, final String pos) {
        if (p == null || raw <= 0L) {
            return;
        }
        try {
            QuaHoatDong.hanhDong(p);
            final List<Char> ds = thanhVienGan(p);
            final int toDoi = CapDoLauDai.heSoToDoi(ds.size());
            final int capQuai = mt != null ? mt.getLevel() : p.lvDetail.lv;
            for (final Char c : ds) {
                final int heLech = CapDoLauDai.heSoLechCap(c.lvDetail.lv - capQuai);
                if (heLech <= 0) {
                    continue;
                }
                final HoSoLauDai hc = LauDaiNhanVat.lay(c);
                final long msTruoc = hc == null ? 0L : hc.nlMsDung;
                final int nl = LauDaiNhanVat.nangLuong(c);
                // Ve EXP (TangToc): nhan SAU nang luong - ve dung luc met chi duoc 20%/5% x he so ve. Chi EXP.
                final int x = HoiSinhGame.apMetExp(c, com.htbb.pokemon.TangToc.nhanVe(
                        CapDoLauDai.expHaQuai(raw, heLech, buffPhanTram(c, ds), nl, LauDaiNhanVat.batKip(c), toDoi),
                        TangTocGame.heSoVe(c, hc, msTruoc))); // Met moi (ve Trung tam sau khi guc): -X% EXP
                if (x > 0) {
                    // nhanExpThu = 1 tren may that (chi may thu + -Dhtbb.thu.nhanExp, xem DongHo)
                    Map.addXPForChar(c, (long) x * com.htbb.pokemon.DongHo.nhanExpThu(), false, pos);
                }
            }
        } catch (final Exception e) {
            System.out.println("ExpLauDai.traoHaQuai " + pos + ": " + e);
        }
    }

    /** p + dong doi o gan, con song, cung map / khu / vung. */
    static List<Char> thanhVienGan(final Char p) {
        final List<Char> ds = new ArrayList<Char>();
        ds.add(p);
        if (p.party == null || p.party.userParty == null) {
            return ds;
        }
        for (int i = 0; i < p.party.userParty.size() && ds.size() < 5; i++) {
            final Char pp;
            try {
                pp = p.party.userParty.get(i);
            } catch (final Exception e) {
                break;
            }
            if (pp == null || pp.id == p.id || pp.hp <= 0) {
                continue;
            }
            if (pp.mapID == p.mapID && pp.inCountry == p.inCountry && pp.region == p.region && p.near(pp, GAN)) {
                ds.add(pp);
            }
        }
        return ds;
    }

    /**
     * Buff EXP cong don (%), CHUA kep (CapDoLauDai kep +100%): pet, canh, mat na, cai trang,
     * ruou, nguoi tuyet, vo chong, ve x2 (1 -> +50, 2.. -> +100), phao hoa, su pham (+20%
     * khi tro chung doi voi thay).
     */
    static int buffPhanTram(final Char c, final List<Char> doi) {
        int b = 0;
        try {
            // Gioi han chi so: nguon tu TRANG BI / pet / thoi trang / cai trang / do giu gop rieng, tran +30%
            // (ChiSoTrangBiGame.expTrangBi), khong cong don qua tran. Nguon su kien / ve / doi giu nguyen.
            int tb = 0;
            if (c.petUsing != null) {
                tb += c.petUsing.getPcUpExp();
            }
            if (c.wModel != null && c.wModel.CanhModel != null) {
                tb += c.wModel.CanhModel.otherAtt[50];
            }
            if (c.wModel != null && c.wModel.matNa != null) {
                tb += c.wModel.matNa.otherAtt[50];
            }
            if (c.isBatGioi()) {
                tb += c.getLevelCaiTrang();
            }
            tb += VatPhamDungLauDai.congExp(c); // Trung may man (do giu)
            b += ChiSoTrangBiGame.expTrangBi(tb);
            if (c.idCuiDot > -1 && c.idCuiDot < 3) {
                b += new int[]{5, 10, 20}[c.idCuiDot];
            }
            if (c.idNgtuyet == 3) {
                b += 20;
            }
            if (Map.x3ExpDotPhao > 0) {
                b += 100;
            }
            if (c.x2Phaohoa > 0) {
                b += 50;
            }
            final int x2 = c.getX2();
            if (x2 == 1) {
                b += 50;
            } else if (x2 >= 2) {
                b += 100;
            }
            b += ChiemGymLauDai.buffExp(c);
            final HoSoLauDai h = LauDaiNhanVat.lay(c);
            if (h != null && h.thayId > 0) {
                for (final Char t : doi) {
                    if (t.charDBID == h.thayId) {
                        b += CuonHut.SU_PHAM_EXP_PHAN_TRAM;
                        break;
                    }
                }
            }
        } catch (final Exception ignored) {
            // thieu model / pet giua chung: bo qua buff do
        }
        return b;
    }

    /**
     * Ban sao cu chia EXP theo lech cap bang downPercent[a] (1/5/10/20). Bat co thi KHONG chia
     * (tra 1) - lech cap moi ap trong traoHaQuai (don muc tieu) hoac lechCapMot (danh lan).
     */
    public static int chiaCu(final int chia) {
        return com.htbb.pokemon.LauDai.bat() ? 1 : chia;
    }

    /**
     * Cong don EXP danh lan (nhieu quai mot don): bat co thi nhan he so lech cap moi cua TUNG
     * quai ngay luc cong (traoHaQuai sau do goi voi mt = null, khong ap lech cap lan 2).
     */
    public static int lechCapMot(final int xp, final Char p, final LiveActor mt) {
        if (!com.htbb.pokemon.LauDai.bat() || p == null || mt == null) {
            return xp;
        }
        return (int) ((long) xp * CapDoLauDai.heSoLechCap(p.lvDetail.lv - mt.getLevel()) / 100L);
    }

    // ------------------------------------------------------------------ len cap / tran

    /**
     * Thay cho "currentXP += dxp / div" cua Map.addXPForChar / addXpCharEvent khi bat co:
     * cat theo tran cap mua, phan thua thanh tinh thong. @return exp moi.
     */
    static long congTheoTran(final Char p, final long expHienTai, final long them) {
        final int tran = CapDoLauDai.tranCap(LauDai.ngayHienTai(), LevelDetail.expMain.length);
        final long[] kq = CapDoLauDai.catTheoTran(expHienTai, them, tran, LevelDetail.expMain);
        if (kq[1] > 0L) {
            LauDaiNhanVat.congTinhThong(p, kq[1], tran);
        }
        return kq[0];
    }

    // ------------------------------------------------------------------ doi bang EXP

    /** Nap bang EXP moi vao LevelDetail (goi luc khoi dong khi bat co, TRUOC khi nhan nguoi choi). */
    public static void apDungBang() {
        if (!LauDai.bat()) {
            return;
        }
        LevelDetail.expMainCu = LevelDetail.expMain;
        LevelDetail.expRequireCu = LevelDetail.expRequire;
        LevelDetail.expMain = CapDoLauDai.expMain(CapDoLauDai.CAP_BANG);
        LevelDetail.expRequire = CapDoLauDai.expRequire(CapDoLauDai.CAP_BANG);
        // Cap 92..100: getBasicPoint(lv) doc allSkillPointLV[lv-1] (90 o) - noi dai theo buoc +10.
        if (LevelDetail.allSkillPointLV.length < CapDoLauDai.CAP_BANG) {
            final int[] cu = LevelDetail.allSkillPointLV;
            final int[] moi = java.util.Arrays.copyOf(cu, CapDoLauDai.CAP_BANG);
            final int buoc = cu.length >= 2 ? cu[cu.length - 1] - cu[cu.length - 2] : 10;
            for (int i = cu.length; i < moi.length; i++) {
                moi[i] = moi[i - 1] + buoc;
            }
            LevelDetail.allSkillPointLV = moi;
        }
        System.out.println("[LAU DAI] bang EXP moi: " + LevelDetail.expMain.length + " cap, tong toi cap "
                + LevelDetail.expMain.length + " = " + LevelDetail.expMain[LevelDetail.expMain.length - 1]);
    }

    // Chuyen EXP nhan vat cu <-> bang moi: real.ChuyenDoiLauDai (tu dong ca hai chieu, 18/10/2026).
}
