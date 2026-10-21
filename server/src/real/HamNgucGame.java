package real;

import com.htbb.pokemon.CuonHut;
import com.htbb.pokemon.HoSoLauDai;
import com.htbb.pokemon.LauDai;
import com.htbb.pokemon.NangLuong;
import com.htbb.pokemon.ThongSoLauDai;
import data.Database;
import data.DatabaseLauDai;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Ham nguc bi an - BAN TOI THIEU (GD2 7.4, THIET_KE 7.5). Chua co ban do / tang rieng (can doi
 * map); moi "tang" la mot thu thach ha quai +-5 cap voi so luong tu hat giong cua ngay
 * (moi nguoi cung de trong ngay), qua tang tu dong sang tang sau. Thuong 1 lan / ngay theo
 * tang cao nhat: 0,2 gio EXP (THIET_KE 5.1 ngan sach "hang_bi_an") + 5 luong khoa tu tang 5.
 */
final class HamNgucGame {

    /** charDBID -> {dangChoi(0/1), tienDo} trong tang hien tai. */
    private static final ConcurrentHashMap<Integer, int[]> DANG = new ConcurrentHashMap<Integer, int[]>();

    private HamNgucGame() {
    }

    static int tienDo(final Char p) {
        final int[] d = DANG.get(Integer.valueOf(p.charDBID));
        return d == null ? 0 : d[1];
    }

    static void batDau(final Char p) {
        DANG.put(Integer.valueOf(p.charDBID), new int[]{1, 0});
    }

    static void khiHaQuai(final Char p, final Monster mt) {
        final int[] d = DANG.get(Integer.valueOf(p.charDBID));
        if (d == null || d[0] == 0 || Math.abs(p.lvDetail.lv - mt.level) > 5) {
            return;
        }
        final HoSoLauDai h = LauDaiNhanVat.lay(p);
        if (h == null) {
            return;
        }
        final int hom = NangLuong.ngay(com.htbb.pokemon.DongHo.bay());
        int qua = -1;
        synchronized (h) {
            if (h.hnNgay != hom) {
                h.hnNgay = hom;
                h.hnTangCao = 0;
                h.hnDaThuong = 0;
            }
            final int tang = h.hnTangCao + 1;
            if (tang > CuonHut.HAM_NGUC_TANG_TOI_DA) {
                return;
            }
            final int[] t = CuonHut.tangHamNguc(hom, tang, new int[]{0});
            d[1]++;
            if (d[1] >= t[1]) {
                h.hnTangCao = tang;
                h.ban = true;
                d[1] = 0;
                qua = tang;
            }
        }
        if (qua > 0) {
            DatabaseLauDai.ghiHamNguc(hom, p.charDBID, qua);
            NhiemVuEngine.datMuc(p, "tang_hang", qua);
            LauDaiNhanVat.bao(p, "Hầm ngục bí ẩn: qua tầng " + qua + "!");
        }
    }

    static String nhanThuong(final Char p, final HoSoLauDai h) {
        final int tang;
        synchronized (h) {
            final boolean homNay = h.hnNgay == NangLuong.ngay(com.htbb.pokemon.DongHo.bay());
            if (homNay && h.hnDaThuong != 0 && h.hnTangCao <= 0) {
                return "Hôm nay đã nhận thưởng Hầm ngục (Phó bản tổ đội, 1 lần/ngày).";
            }
            if (!homNay || h.hnTangCao <= 0) {
                return "Hôm nay chưa qua tầng nào.";
            }
            if (h.hnDaThuong != 0) {
                // nap tien: the thang +1 luot - CHI diem ve mua (khong EXP / vat lieu / luong).
                final String them = com.htbb.pokemon.NapTienGame.luotHamNgucThem(p);
                return them != null ? them : "Hôm nay đã nhận thưởng Hầm ngục bí ẩn (1 lần/ngày).";
            }
            h.hnDaThuong = 1; // danh dau truoc
            h.ban = true;
            tang = h.hnTangCao;
        }
        final long exp = ThongSoLauDai.expMoiGio(LauDai.kepCap(p.lvDetail.lv)) * 20L / 100L * Math.min(10, tang) / 10L;
        final int lk = tang >= 5 ? 5 : 0;
        try {
            Map.addXPForChar(p, Math.max(1L, exp), false, "ham nguc");
        } catch (final Exception ignored) {
        }
        if (lk > 0) {
            p.addLuongLock(lk);
        }
        Database.instance.saveOrtherLog("", p.charname, "ham nguc tang " + tang + " exp " + exp + " lk " + lk, "laudai");
        return "Thưởng Hầm ngục bí ẩn (tầng " + tang + "): " + exp + " EXP" + (lk > 0 ? ", " + com.htbb.pokemon.TienTeChu.chu(com.htbb.pokemon.TienTeChu.LUONG_KHOA, lk) : "") + ".";
    }
}
