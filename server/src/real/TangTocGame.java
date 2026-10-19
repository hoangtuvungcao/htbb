package real;

import com.htbb.pokemon.HoSoLauDai;
import com.htbb.pokemon.LauDai;
import com.htbb.pokemon.NangLuong;
import com.htbb.pokemon.NhiemVuLauDai;
import com.htbb.pokemon.TangToc;
import com.htbb.pokemon.ThongSoLauDai;
import data.Database;

/**
 * Noi vat pham TANG TOC (com.htbb.pokemon.TangToc) vao game - chi khi sv.lau_dai=1. Thiet ke:
 * docs/thiet-ke-game.md.
 *
 *   - dung: VatPhamDungLauDai.dung re nhanh cho id 1250..1257 (ve x1,5 / x2, bua the luc, uy thac; ban cao cap va
 *     ban khoa). Tru vat pham SAU khi kiem tran, TRUOC khi ap hieu ung (CODING_CONVENTION muc 5).
 *   - ExpLauDai.traoHaQuai: {@link #heSoVe} nhan EXP ha quai khi ve dang chay (chi EXP).
 *   - Uy thac: gio dat truoc chay khi THOAT game; dang nhap lai -> EXP cho, phat o vong nhip (LauDaiJob) khi
 *     nhan vat da vao ban do.
 *   - Nguon ban khoa: {@link #thuongKhoa} (nhiem vu tuan, lich dang nhap, ve mua) + Kecleon (luong khoa, du lieu).
 */
public final class TangTocGame {

    private TangTocGame() {
    }

    static int homNay(final long bay) {
        return NangLuong.ngay(bay);
    }

    static int tuanNay(final long bay) {
        return NhiemVuLauDai.tuan(bay);
    }

    /** The thang con han (cong +1 gio uy thac/ngay). Loi / tat nap = false. */
    static boolean coTheThang(final Char p) {
        try {
            return com.htbb.pokemon.NapTien.bat() && com.htbb.pokemon.NapTienGame.banKinhNhatThemPx(p) > 0;
        } catch (final Exception e) {
            return false;
        }
    }

    // ------------------------------------------------------------------ dung vat pham

    /** VatPhamDungLauDai.dung: @return true neu {@code item} la vat pham tang toc (da xu ly, ke ca tu choi). */
    public static boolean dung(final Char p, final Item item) {
        if (!LauDai.bat() || p == null || item == null || item.getTemplate() == null) {
            return false;
        }
        final int loai = TangToc.loaiCuaItem(item.getTemplate().id);
        if (loai < 0 || loai == TangToc.KEO) {
            return false; // Keo hiem: luong cu cua VatPhamDungLauDai (co kiem tran qua kiemKeo/ghiKeo)
        }
        final HoSoLauDai h = LauDaiNhanVat.lay(p);
        if (h == null) {
            VatPhamDungLauDai.bao(p, "Tạm thời không dùng được, thử lại sau.");
            return true;
        }
        final long bay = System.currentTimeMillis();
        final int ngay = homNay(bay), tuan = tuanNay(bay);
        final String loi = TangToc.kiemDung(h, loai, p.lvDetail.lv, LevelDetail.expMain.length, coTheThang(p), ngay, tuan);
        if (loi != null) {
            VatPhamDungLauDai.bao(p, loi);
            return true;
        }
        if (!p.iItems.remove(item)) {
            return true;
        }
        if (item.dbid != 0) {
            try {
                Database.instance.deleteItem(item.dbid);
            } catch (final Exception ignored) {
            }
        }
        // Bua: chua danh quai hom nay -> TangToc ghi "cho", NangLuong cong khi doi ngay (khong cong nham vao hom qua).
        TangToc.ghiDung(h, loai, ngay, tuan);
        p.sendMessage(MessageCreator.createCharInventoryMessage(p, 1));
        try {
            Database.instance.saveOrtherLog("tob_log_other_item", p.charname, "tang toc " + TangToc.MA[loai] + " id"
                    + item.getTemplate().id + " dbid" + item.dbid, "tangtoc");
        } catch (final Exception ignored) {
        }
        final String them;
        if (loai == TangToc.BUA) {
            them = "+" + TangToc.BUA_PHUT + " phút năng lượng 100% hôm nay (chỉ EXP).";
        } else if (loai == TangToc.UY_THAC) {
            them = "Đặt trước 1 giờ ủy thác: thoát game để Pokémon tự luyện (" + TangToc.UY_THAC_PHAN_TRAM
                    + "% EXP 1 giờ đánh quái, không có xu/vật phẩm).";
        } else {
            them = "Vé chạy khi đánh quái (đứng yên không mất).";
        }
        VatPhamDungLauDai.bao(p, "Đã dùng " + item.getTemplate().name + ". " + them + "\n" + TangToc.trangThai(h, ngay, tuan));
        return true;
    }

    /** Keo hiem (VatPhamDungLauDai): ly do chan theo tran tang toc / cap toi da, null neu duoc. */
    static String kiemKeo(final Char p) {
        final HoSoLauDai h = LauDaiNhanVat.lay(p);
        if (h == null) {
            return null;
        }
        final long bay = System.currentTimeMillis();
        return TangToc.kiemDung(h, TangToc.KEO, p.lvDetail.lv, LevelDetail.expMain.length, false, homNay(bay), tuanNay(bay));
    }

    /** Keo hiem da dung: tru diem tang toc. */
    static void ghiKeo(final Char p) {
        final HoSoLauDai h = LauDaiNhanVat.lay(p);
        if (h != null) {
            final long bay = System.currentTimeMillis();
            TangToc.ghiDung(h, TangToc.KEO, homNay(bay), tuanNay(bay));
        }
    }

    // ------------------------------------------------------------------ ve EXP khi ha quai

    /**
     * He so ve (%) cho {@code c} o lan ha quai nay. {@code msTruoc} = h.nlMsDung TRUOC khi LauDaiNhanVat.nangLuong
     * ghi nhan lan nay (phan chenh = thoi gian danh quai vua tinh). Cap toi da: 100 (ve dung lai).
     */
    static int heSoVe(final Char c, final HoSoLauDai h, final long msTruoc) {
        if (h == null || h.tgVeMs <= 0L || c.lvDetail.lv >= LevelDetail.expMain.length) {
            return 100;
        }
        final long sau = h.nlMsDung;
        final long vua = sau >= msTruoc ? sau - msTruoc : sau; // doi ngay: bo dem ve 0
        return TangToc.heSoVeVaTru(h, vua);
    }

    // ------------------------------------------------------------------ uy thac (offline)

    /** Goi tu LauDaiNhanVat.luu (moi lan luu nhan vat): ghi moc "thoat" khi co gio uy thac dang cho. */
    static void ghiMocLuu(final HoSoLauDai h, final long bay) {
        synchronized (h) {
            if (h.tgUtMs > 0L) {
                h.tgThoat = bay;
                h.ban = true;
            }
        }
    }

    /** EXP uy thac cho phat (charDBID -> exp), tinh luc nap nhan vat, phat o vong nhip. */
    private static final java.util.concurrent.ConcurrentHashMap<Integer, Long> CHO = new java.util.concurrent.ConcurrentHashMap<Integer, Long>();

    /** LauDaiNhanVat.khiNap: tinh gio uy thac da chay trong luc offline. */
    static void khiNap(final Char p, final HoSoLauDai h) {
        final long bay = System.currentTimeMillis();
        final long ms = TangToc.tinhUyThac(h, h.tgThoat, bay, homNay(bay), tuanNay(bay));
        if (ms <= 0L || p.lvDetail.lv >= LevelDetail.expMain.length) {
            return;
        }
        final long exp = TangToc.expUyThac(ms, ThongSoLauDai.expMoiGio(LauDai.kepCap(p.lvDetail.lv)));
        if (exp > 0L) {
            CHO.put(Integer.valueOf(p.charDBID), Long.valueOf(exp));
        }
    }

    /** VatPhamDungLauDai.nhip (moi 2-3 giay): phat EXP uy thac da tinh cho nhan vat dang online. */
    static void nhip(final Char p) {
        final Long exp = CHO.remove(Integer.valueOf(p.charDBID));
        if (exp == null || exp.longValue() <= 0L) {
            return;
        }
        try {
            Map.addXPForChar(p, exp.longValue(), false, "uy thac");
            VatPhamDungLauDai.bao(p, "Pokémon đã tự luyện (ủy thác) trong lúc bạn vắng mặt, +" + exp + " EXP.");
            Database.instance.saveOrtherLog("tob_log_other_item", p.charname, "uy thac +" + exp + " exp", "tangtoc");
        } catch (final Exception e) {
            System.out.println("TangTocGame.nhip: " + e);
        }
    }

    // ------------------------------------------------------------------ nguon ban khoa (choi)

    /**
     * Trao ban KHOA cua mon {@code loai} (x {@code so}) - nguon choi: nhiem vu tuan, lich dang nhap, ve mua.
     * Het cho trong tui -> bo qua (khong ra tien te thay the).
     */
    public static void thuongKhoa(final Char p, final int loai, final int so, final String nguon) {
        if (!LauDai.bat() || p == null || loai < 0 || loai >= TangToc.SO_LOAI || so <= 0) {
            return;
        }
        try {
            final int id = TangToc.ID_KHOA[loai];
            if (Map.itemTemplates.get(5).get(Integer.valueOf(id)) == null) {
                return; // chua chay sql/nang_cap.sql
            }
            if (p.countSlotInventory() < so) {
                VatPhamDungLauDai.bao(p, "Hành trang đầy: không nhận được " + TangToc.TEN[loai] + " (" + nguon + ").");
                return;
            }
            Char.createItemSoLuong(id, so, p);
            p.sendMessage(MessageCreator.createCharInventoryMessage(p, 0));
            Database.instance.saveOrtherLog("tob_log_other_item", p.charname, "tang toc khoa " + TangToc.MA[loai] + " x" + so
                    + " tu " + nguon, "tangtoc");
        } catch (final Exception e) {
            System.out.println("TangTocGame.thuongKhoa: " + e);
        }
    }
}
