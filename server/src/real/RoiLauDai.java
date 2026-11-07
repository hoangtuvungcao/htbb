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
        MucTieuNhiemVuGame.khiHaQuai(p, mt); // sua-tien-do-nv: ha quai cua ai chua nhan -> hop 95 mot lan
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
                com.htbb.thongbao.ThongBao.rieng(p, 
                        "Bạn vừa thấy một " + PokedexPokemon.tenLoai(dex) + " shiny! Đã ghi vào Pokédex.");
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
        return phanRa(p, it, false);
    }

    /** Bot thuoc tinh dung cho "Thêm dòng" (GemTemplate.TYPE_BOT, MapCheTao.doAddNewAttributeUseBot). */
    static final int ID_BOT_THEM_DONG = 249;
    /** "Đá thêm dòng" (GemTemplate.TYPE_DA_TIEN_GIAI, ten cu "Đá thăng bậc"). */
    static final int ID_DA_THEM_DONG = 250;

    /**
     * Ti le (phan nghin) ra {Bot xanh la, Da them dong} khi phan ra do theo do hiem {@code colorName}. Thêm dòng (Tho ren)
     * chi dung cho do Hoan my nen do Hoan my LUON ra du 1 bo; Do / Xanh co co hoi; Trang khong ra.
     *
     * <p>Sua 08/11/2026 (bao loi Đào Ngọc Thịnh / rongngoo, 6 lan phan ra 07/11 22:28): "Phân giải trang bị ra xong không
     * nhận được cái đồ luyện thêm dòng". Tu khi bat dai han (LauDai.bat) phan ra CHI ra 2 Da nang cap + 10% gia xu - bot
     * (truoc la 246 "Bột trắng", ma 246 cung khong phai TYPE_BOT nen ca ban cu cung khong dung duoc) va da them dong KHONG
     * con nguon nao (Monster: co BotXanhLa = false), Thêm dòng o Tho ren thanh chuc nang chet. Ket qua lai hien icon 72
     * (Bột trắng) voi so = so da -> nguoi choi tuong nhan bot.
     */
    static int[] tiLeThemDong(final int colorName) {
        if (colorName == Item.COLOR_GREEN) { // Hoan my
            return new int[]{1000, 1000};
        }
        if (colorName == Item.COLOR_RED) {
            return new int[]{300, 150};
        }
        if (colorName == Item.COLOR_BLUE) {
            return new int[]{150, 50};
        }
        return new int[]{0, 0};
    }

    static String phanTram(final int phanNghin) {
        return phanNghin % 10 == 0 ? (phanNghin / 10) + "%" : (phanNghin / 10.0) + "%";
    }

    /** Ten ngoc {@code id} theo bang dang nap, {@code macDinh} neu chua co. */
    static String tenNgoc(final int id, final String macDinh) {
        try {
            final String t = Map.gemTemplate[id].name;
            return t == null || t.trim().isEmpty() ? macDinh : t.trim();
        } catch (final Exception e) {
            return macDinh;
        }
    }

    /**
     * Phan ra {@code it}: 2 da + 10% gia xu + nguyen lieu Thêm dòng theo {@link #tiLeThemDong}. Bao cho nguoi choi DUNG
     * nhung gi da nhan va ti le cua phan khong ra: {@code tay} (nghien tay o Tho ren) = thong bao nhanh; tu dong (nhat do)
     * = mot dong tin nhan (khong bat hop thoai moi lan nhat). @return so da.
     */
    static int phanRa(final Char p, final Item it, final boolean tay) {
        final int gia = Map.itemTemplates.get(5).get(it.getTemplate().id) != null
                ? Map.itemTemplates.get(5).get(it.getTemplate().id).price : it.getTemplate().price;
        final long[] kq = KinhTeLauDai.phanRa(gia);
        final boolean khoa = it.lock == 1;
        p.doAddGemItem(ID_DA, (int) kq[0], khoa);
        NhiemVuEngine.suKien(p, "phan_ra", "", 1);
        if (kq[1] > 0L) {
            p.addXu(kq[1], "phan ra do lau dai");
        }
        final int[] tl = tiLeThemDong(it.colorName);
        final boolean raBot = tl[0] > 0 && (tl[0] >= 1000 || Map.r.nextInt(1000) < tl[0]);
        final boolean raDa = tl[1] > 0 && (tl[1] >= 1000 || Map.r.nextInt(1000) < tl[1]);
        if (raBot) {
            p.doAddGemItem(ID_BOT_THEM_DONG, 1, khoa);
        }
        if (raDa) {
            p.doAddGemItem(ID_DA_THEM_DONG, 1, khoa);
        }
        final String s = moTaPhanRa(DungVatPham.ten(it), (int) kq[0], kq[1], tl, raBot, raDa, khoa);
        try {
            // game.out (tob_log_all_item chi giu loai trong LocLogVatPham.GIU): tra cuu khi nguoi choi hoi "phan ra ra gi"
            com.htbb.log.Nhat.thongTin("PHAN_RA", p.charname + " tpl " + it.getTemplate().id + " mau " + it.colorName
                    + (tay ? " tay" : " tu dong") + ": da " + kq[0] + " xu " + kq[1] + " bot " + (raBot ? 1 : 0)
                    + " da_them_dong " + (raDa ? 1 : 0));
        } catch (final Exception ignored) {
        }
        try {
            if (tay) {
                DungVatPham.bao(p, s);
            } else {
                p.sendMessage(MessageCreator.createMsgChat(p.id, s));
            }
        } catch (final Exception ignored) {
        }
        return (int) kq[0];
    }

    /** Chuoi ket qua phan ra: liet ke dung phan da nhan + ti le phan khong ra (thuan, de kiem). */
    static String moTaPhanRa(final String ten, final int da, final long xu, final int[] tl, final boolean raBot,
            final boolean raDa, final boolean khoa) {
        final String bot = tenNgoc(ID_BOT_THEM_DONG, "Bột xanh lá");
        final String daTd = tenNgoc(ID_DA_THEM_DONG, "Đá thêm dòng");
        final StringBuilder sb = new StringBuilder("Phân rã ").append(ten).append(": +").append(da).append(" Đá nâng cấp");
        if (xu > 0L) {
            sb.append(", +").append(xu).append(" xu");
        }
        if (raBot) {
            sb.append(", +1 ").append(bot);
        }
        if (raDa) {
            sb.append(", +1 ").append(daTd);
        }
        if (khoa && (da > 0 || raBot || raDa)) {
            sb.append(" (khoá)");
        }
        sb.append('.');
        if (tl[0] <= 0 && tl[1] <= 0) {
            sb.append(" Đồ Trắng không ra nguyên liệu Thêm dòng (đồ Xanh / Đỏ có tỉ lệ, đồ Hoàn mỹ chắc chắn có).");
        } else if (!raBot || !raDa) {
            sb.append(" Lần này không ra");
            if (!raBot) {
                sb.append(' ').append(bot).append(" (tỉ lệ ").append(phanTram(tl[0])).append(')');
            }
            if (!raDa) {
                sb.append(raBot ? " " : ", ").append(daTd).append(" (tỉ lệ ").append(phanTram(tl[1])).append(')');
            }
            sb.append("; đồ Hoàn mỹ chắc chắn có cả hai.");
        }
        if (raBot || raDa) {
            sb.append(" Bột + Đá thêm dòng dùng ở Thợ rèn › Thêm dòng (đồ Hoàn mỹ).");
        }
        return sb.toString();
    }

    static int xuMoiQuai(final int cap) {
        return ThongSoLauDai.XU_MOI_QUAI[Math.max(1, Math.min(100, cap))];
    }
}
