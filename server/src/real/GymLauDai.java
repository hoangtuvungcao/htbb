package real;

import com.htbb.pokemon.GiaiDoanBoss;
import com.htbb.pokemon.HoSoLauDai;
import com.htbb.pokemon.LauDai;
import com.htbb.pokemon.NhiemVuLauDai;
import data.Database;
import data.DatabaseLauDai;
import java.util.ArrayList;
import java.util.List;

/**
 * 16 thu linh Gym (GD2b, NOI_DUNG_DAY_DU.md muc 6; data_noi_dung_gym + data_noi_dung_roi nguon
 * gym / gym_tuan). Vao tu So tay huan luyen -> "Thách đấu Gym": trieu hoi boss tai cho, 1 dau 1 (chi
 * nguoi thach dau gay duoc sat thuong), no sau no_sau_phut phut (GiaiDoanBoss), het 10 phut chua ha
 * thi thua. Lan dau: huy hieu (bit trong ho so) + 1 vu khi Hoan my dung dong. Danh lai: 1 lan / tuan,
 * 3 Huy chuong Gym (item 1039, khoa).
 */
public final class GymLauDai {

    static final long THOI_GIAN_MS = 10L * 60L * 1000L;

    /** {stt, ma, huyHieu, cap, template, tenBoss, hp, tc, thu, noPhut, thuongDau, thuongTuan}. */
    static volatile List<Object[]> GYM = new ArrayList<Object[]>();

    private GymLauDai() {
    }

    static void nap() {
        GYM = DatabaseLauDai.napGym();
    }

    static int so(final Object o) {
        return o instanceof Number ? ((Number) o).intValue() : 0;
    }

    static Object[] gym(final int stt) {
        for (final Object[] g : GYM) {
            if (so(g[0]) == stt) {
                return g;
            }
        }
        return null;
    }

    public static boolean coHuyHieu(final HoSoLauDai h, final int stt) {
        return h != null && stt >= 1 && stt <= 30 && (h.gymHuyHieu & (1 << (stt - 1))) != 0;
    }

    static void moMenu(final Char p) {
        final HoSoLauDai h = LauDaiNhanVat.lay(p);
        if (h == null || GYM.isEmpty()) {
            SoTayLauDai.bao(p, "Chưa có dữ liệu Gym.");
            return;
        }
        final String[] menu = new String[GYM.size()];
        for (int i = 0; i < menu.length; i++) {
            final Object[] g = GYM.get(i);
            final int stt = so(g[0]);
            menu[i] = g[2] + " - cấp " + so(g[3]) + (coHuyHieu(h, stt) ? " (đã có)" : "");
        }
        p.sendMessage(MessageCreator.createMsgMenu(menu, SoTayLauDai.NPC_ID, SoTayLauDai.MENU_GYM));
    }

    /** Chon Gym thu {@code i} trong menu. */
    static String thachDau(final Char p, final int i) {
        if (!com.htbb.cauhinh.CongTac.gym()) {
            return "Gym đang tạm đóng. Vui lòng quay lại sau."; // cau hinh trung tam: gym.bat=0
        }
        if (i < 0 || i >= GYM.size()) {
            return "Gym không hợp lệ.";
        }
        final Object[] g = GYM.get(i);
        final int stt = so(g[0]);
        final HoSoLauDai h = LauDaiNhanVat.lay(p);
        if (h == null) {
            return "Tạm thời không thách đấu được.";
        }
        if (p.lvDetail.lv < so(g[3])) {
            return "Cần đạt cấp " + so(g[3]) + ".";
        }
        if (BossLauDai.gymDangDanh(p) > 0) {
            return "Bạn đang thách đấu một Gym.";
        }
        final int tuan = NhiemVuLauDai.tuan(com.htbb.pokemon.DongHo.bay());
        synchronized (h) {
            if (h.gymTuan != tuan) {
                h.gymTuan = tuan;
                h.gymTuanBit = 0;
            }
            if (coHuyHieu(h, stt) && (h.gymTuanBit & (1 << (stt - 1))) != 0) {
                return "Tuần này đã đánh lại Gym này rồi.";
            }
        }
        final MonsterTemplate t = Map.monsterTemplates.get(Integer.valueOf(so(g[4])));
        if (t == null || p.map == null) {
            return "Không triệu hồi được trưởng Gym.";
        }
        final Monster b = BossLauDai.trieuHoi(p.map, t, p.x + 48, p.y, p.inCountry, p.region, so(g[3]), so(g[6]), so(g[7]), so(g[8]));
        if (b == null) {
            return "Không triệu hồi được trưởng Gym.";
        }
        BossLauDai.datGym(b, stt, p, THOI_GIAN_MS);
        try {
            Database.instance.saveOrtherLog("", p.charname, "thach dau gym " + stt, "laudai");
        } catch (final Exception ignored) {
        }
        return g[5] + " xuất hiện! Hãy hạ trong 10 phút (nổi giận sau " + so(g[9]) + " phút).";
    }

    /** BossLauDai.khiChet: boss Gym {@code stt} cua {@code idChar} bi ha. */
    static void khiHa(final int stt, final int idChar) {
        Char p = null;
        for (int i = 0; i < CharManager.instance.vChars.size(); i++) {
            final Char c = CharManager.instance.vChars.elementAt(i);
            if (c != null && c.charDBID == idChar) {
                p = c;
                break;
            }
        }
        final Object[] g = gym(stt);
        if (p == null || g == null) {
            return;
        }
        final HoSoLauDai h = LauDaiNhanVat.lay(p);
        if (h == null) {
            return;
        }
        final boolean lanDau;
        synchronized (h) {
            lanDau = !coHuyHieu(h, stt);
            if (lanDau) {
                h.gymHuyHieu |= 1 << (stt - 1);
            } else {
                if ((h.gymTuanBit & (1 << (stt - 1))) != 0) {
                    return;
                }
                h.gymTuanBit |= 1 << (stt - 1);
            }
            h.ban = true; // danh dau TRUOC, phat SAU
        }
        NhiemVuEngine.suKien(p, "ha_gym", (String) g[1], 1);
        NhiemVuEngine.suKien(p, "ha_gym", String.valueOf(stt), 1);
        if (lanDau) {
            vuKhiHoanMy(p, (String) g[1]);
            LauDaiNhanVat.bao(p, "Chiến thắng! Nhận " + g[2] + ".");
        } else {
            for (final Object[] r : NoiDungLauDai.ROI) {
                if ("gym_tuan".equals(r[0]) && g[1].equals(r[1])) {
                    NoiDungLauDai.phat(p, (String) r[2], ((Integer) r[3]).intValue(), ((Integer) r[5]).intValue(), true);
                }
            }
            LauDaiNhanVat.bao(p, "Đánh lại Gym thành công: nhận Huy chương Gym.");
        }
        try {
            Database.instance.saveOrtherLog("", p.charname, "ha gym " + stt + (lanDau ? " lan dau" : " tuan"), "laudai");
        } catch (final Exception ignored) {
        }
    }

    /** Vu khi Hoan my DUNG DONG nhan vat trong cac dong roi "gym" cua ma nhiem vu. */
    static void vuKhiHoanMy(final Char p, final String ma) {
        final List<Integer> ds = new ArrayList<Integer>();
        for (final Object[] r : NoiDungLauDai.ROI) {
            if ("gym".equals(r[0]) && ma.equals(r[1]) && "item".equals(r[2])) {
                ds.add((Integer) r[3]);
            }
        }
        if (ds.isEmpty()) {
            return;
        }
        // 5 dong theo thu tu Than lua / Hat giong / Khan lua / Nuoc than / Nam cham = class 0..4
        final int id = ds.get(Math.max(0, Math.min(ds.size() - 1, p.charClass))).intValue();
        try {
            final Item it = Map.dropItemByIdTemplate(p.inCountry, id);
            if (it == null || p.isFullInventory()) {
                LauDaiNhanVat.bao(p, "Hành trang đầy, không nhận được đồ cầm tay Hoàn mỹ.");
                return;
            }
            it.id = p.getIDItem();
            it.owner = p.charDBID;
            it.colorName = Item.COLOR_GREEN; // Hoan my (Item.java: ten mau 1 = "Hoàn mỹ")
            it.lock = 1;
            p.iItems.add(it);
            p.sendMessage(MessageCreator.createCharInventoryMessage(p, 0));
        } catch (final Exception e) {
            System.out.println("GymLauDai.vuKhiHoanMy: " + e);
        }
    }

    /** No sau (phut) cua Gym - dung GiaiDoanBoss.NO_GYM_MS mac dinh. */
    static long noMs() {
        return GiaiDoanBoss.NO_GYM_MS;
    }

    static boolean bat() {
        return LauDai.bat();
    }
}
