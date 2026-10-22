package real;

import com.htbb.pokemon.CapDoLauDai;
import com.htbb.pokemon.CuonHut;
import com.htbb.pokemon.HoSoLauDai;
import com.htbb.pokemon.KinhTeLauDai;
import com.htbb.pokemon.ThongSoLauDai;
import data.Database;
import data.DatabaseLauDai;
import java.util.Vector;

/**
 * Roi do khi ha quai theo thiet ke dai han (GD2 2.2/2.3, 7.3). Goi tu Monster.onDropItem khi
 * LauDai.bat(), THAY khoi xu cu. Nang luong da ghi nhan o ExpLauDai (cung lan ha) - o day chi
 * doc he so hien tai, khong ghi lan 2.
 */
final class RoiLauDai {

    /** Ngoc luyen kim thap nhat (8..11 = luyen kim duoc, xem MapEpNgoc.haveGem) lam "da nang cap". */
    static final int ID_DA = 8;

    private RoiLauDai() {
    }

    static void roi(final Monster mt, final Map m, final Char p, final Vector<Actor> droplist) {
        if (p.receiveXP() == 3) {
            return;
        }
        final int delta = p.lvDetail.lv - mt.level;
        final int heLech = CapDoLauDai.heSoLechCap(delta);
        final int nl = LauDaiNhanVat.nangLuongHienTai(p);
        if (heLech > 0 && nl > 0 && Map.randomMillion() < KinhTeLauDai.tiLeRoiXuPhanTrieu()) {
            final int sl = KinhTeLauDai.xuMotLanRoi(mt.level, heLech, nl);
            if (sl > 0) {
                final Potion pt = new Potion((short) 0, sl, m);
                pt.id = m.getIDITEM();
                pt.x = mt.x;
                pt.y = mt.y;
                pt.belongUser = p.charDBID;
                m.addPotion(pt, mt.inCountry);
                pt.time_drop = System.currentTimeMillis();
                droplist.add(pt);
                if (p.autoGetItem == 1) {
                    p.idPotion.add(pt.id);
                }
            }
        }
        NoiDungLauDai.roiBang(mt, p, nl); // GD2b: bang roi data_noi_dung_roi (dai cap / alpha)
        if (nl > 0 && Map.randomMillion() < KinhTeLauDai.tiLeRoiDaPhanTrieu(delta, nl)) {
            p.doAddGemItem(ID_DA, 1, false);
            p.sendMessage(MessageCreator.createCharGemItem(p));
        }
        shiny(mt, p);
        NhiemVuLauDaiGame.khiHaQuai(p, mt);
        HamNgucGame.khiHaQuai(p, mt);
        NhiemVuEngine.khiHaQuai(p, mt); // GD2b engine nhiem vu
    }

    /** Shiny 1/4096 (x su kien), pity 2000 lan ha (GD2 7.3). Chi danh hieu/Pokedex, khong suc manh. */
    static void shiny(final Monster mt, final Char p) {
        final HoSoLauDai h = LauDaiNhanVat.lay(p);
        if (h == null) {
            return;
        }
        final int dex = PokedexPokemon.dexQuai(p, mt);
        if (dex <= 0) {
            return;
        }
        // ha dung con quai SAC MAU nguoi nay nhin thay (loai rieng shiny, client moi): ghi "thay shiny" vao Pokedex ngay,
        // khong dung toi dem pity (dem pity chi cho shiny tung luc ha nhu cu)
        final boolean thayShiny = LoaiQuaiGame.shinyChoNguoi(p, mt) && dex == mt.dexRieng;
        final boolean ra;
        synchronized (h) {
            if (thayShiny) {
                h.shinySo++;
                h.ban = true;
            }
            ra = !thayShiny && CuonHut.shiny(h.shinyDem, Map.random(CuonHut.SHINY_MAU), SuKienLauDaiGame.nhanShiny(),
                    CuonHut.SHINY_MAU, CuonHut.SHINY_PITY);
            if (!thayShiny) {
                h.shinyDem = ra ? 0 : h.shinyDem + 1;
            }
            if (ra) {
                h.shinySo++;
            }
            h.ban = true;
        }
        if (ra || thayShiny) {
            DatabaseLauDai.congShiny(p.charDBID, dex);
            NhiemVuEngine.suKien(p, "pokedex_shiny", String.valueOf(dex), 1);
            try {
                p.sendMessage(MessageCreator.createServerAlertAutoOffMessage(
                        "Bạn vừa thấy một " + PokedexPokemon.tenLoai(dex) + " shiny! Đã ghi vào Pokédex."));
            } catch (final Exception ignored) {
            }
            Database.instance.saveOrtherLog("", p.charname, "shiny dex " + dex, "laudai");
        }
    }

    /**
     * Phan ra do (GD2 2.9, THIET_KE 3.5): 2 da luyen kim + 10% gia xu, khong mat phi. Dung cho
     * nghien tay (OfflineMap) va nghien tu dong (MapXuLyLenh) khi bat co. @return so da.
     */
    static int phanRa(final Char p, final Item it) {
        final int gia = Map.itemTemplates.get(5).get(it.getTemplate().id) != null
                ? Map.itemTemplates.get(5).get(it.getTemplate().id).price : it.getTemplate().price;
        final long[] kq = KinhTeLauDai.phanRa(gia);
        p.doAddGemItem(ID_DA, (int) kq[0], it.lock == 1);
        NhiemVuEngine.suKien(p, "phan_ra", "", 1);
        if (kq[1] > 0L) {
            p.addXu(kq[1], "phan ra do lau dai");
        }
        return (int) kq[0];
    }

    static int xuMoiQuai(final int cap) {
        return ThongSoLauDai.XU_MOI_QUAI[Math.max(1, Math.min(100, cap))];
    }
}
