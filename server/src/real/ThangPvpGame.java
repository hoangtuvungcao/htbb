package real;

import com.htbb.pokemon.HoSoLauDai;
import com.htbb.pokemon.LauDai;
import com.htbb.pokemon.ThangPvp;
import data.DatabaseLauDai;

/**
 * Thang PvP mua (GD2 7.2). 0.9.7 (sau ra soat loi choi): Elo CHI doi trong TRAN XEP HANG do may chu ghep
 * (real.XepHangPvpGame: hang cho NPC Dau truong, lech <= 10 cap + Elo, khac tai khoan / thiet bi / IP, tran
 * &gt;= 20 giay va ben thua phai danh tra; cung doi thu trong 24 gio Elo giam 1 -&gt; 1/2 -&gt; 1/4 -&gt; 0). Giet nguoi ngoai
 * the gioi KHONG con doi Elo (truoc day cay duoc bang nick phu). Mua 13 tuan (ThangPvp.mua), sang mua moi reset mem
 * 50%, thuong mua phat luc dang nhap mua sau (XepHangPvpGame.thuongMuaCu). Luu tob_pvp_mua + ho so.
 */
public final class ThangPvpGame {

    private ThangPvpGame() {
    }

    public static void ketQua(final Char thang, final Char thua) {
        if (!LauDai.bat() || thang == null || thua == null || thang.charDBID == thua.charDBID) {
            return;
        }
        if (!com.htbb.cauhinh.CongTac.pvpTinhDiem()) {
            return; // cau hinh trung tam: pvp.tinh_diem.bat=0
        }
        try {
            final int heSo = XepHangPvpGame.ketQua(thang, thua);
            if (heSo < 0) {
                return; // khong phai tran xep hang: khong Elo, khong tinh thang PvP
            }
            if (heSo == 0) {
                return; // tran xep hang bi huy (qua ngan / khong danh tra / cung may) hoac het he so voi doi thu nay
            }
            final HoSoLauDai a = LauDaiNhanVat.lay(thang);
            final HoSoLauDai b = LauDaiNhanVat.lay(thua);
            if (a == null || b == null) {
                return;
            }
            final int mua = ThangPvp.mua(LauDai.ngayHienTai());
            doiMua(a, mua);
            doiMua(b, mua);
            final int ea = a.pvpElo;
            final int eb = b.pvpElo;
            synchronized (a) {
                a.pvpElo = com.htbb.pokemon.XepHangPvp.eloMoi(ea, eb, 1, heSo);
                a.pvpThang++;
                a.ban = true;
            }
            synchronized (b) {
                b.pvpElo = com.htbb.pokemon.XepHangPvp.eloMoi(eb, ea, 0, heSo);
                b.pvpThua++;
                b.ban = true;
            }
            DatabaseLauDai.ghiPvp(mua, thang.charDBID, a.pvpElo, a.pvpThang, a.pvpThua);
            DatabaseLauDai.ghiPvp(mua, thua.charDBID, b.pvpElo, b.pvpThang, b.pvpThua);
            XepHangPvpGame.ghiTran(mua, thang.charDBID, thua.charDBID, true, "", 0L, a.pvpElo, b.pvpElo);
            NhiemVuEngine.suKien(thang, "thang_pvp", "", 1);
            NhiemVuEngine.suKien(thang, "thanh_tuu_pvp", "", 1);
            com.htbb.thongbao.ThongBao.gui(thang, com.htbb.thongbao.ThongBao.KET_QUA, "Thắng trận xếp hạng",
                    "Elo " + ea + " → " + a.pvpElo + (heSo < 1000 ? " (gặp lại đối thủ: ×" + heSo / 10 + "%)" : ""),
                    com.htbb.thongbao.ThongBao.HINH_KHONG, 0);
            com.htbb.thongbao.ThongBao.gui(thua, com.htbb.thongbao.ThongBao.TIN, "Thua trận xếp hạng",
                    "Elo " + eb + " → " + b.pvpElo, com.htbb.thongbao.ThongBao.HINH_KHONG, 0);
        } catch (final Exception e) {
            System.out.println("ThangPvpGame.ketQua: " + e);
        }
    }

    static void doiMua(final HoSoLauDai h, final int mua) {
        synchronized (h) {
            if (h.pvpMua != mua) {
                h.pvpElo = ThangPvp.resetMem(h.pvpElo);
                h.pvpThang = 0;
                h.pvpThua = 0;
                h.pvpMua = mua;
                h.ban = true;
            }
        }
    }
}
