import com.htbb.pokemon.LauDai;
import com.htbb.pokemon.NhiemVuLauDai;
import com.htbb.pokemon.NhiemVuMau;

import data.InfoQuestTemplate;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;
import real.Char;
import real.Map;
import real.NewCharQuest;
import real.NhiemVuEngine;
import real.NhiemVuLauDaiGame;

/**
 * NHIEM VU PHU MO RONG (30/10/2026, chu game): phu moi khi cap nhiem vu <= cap nguoi choi + 10, KHONG chan duoi (truoc day
 * chi trong 10 cap duoi: nguoi len cap nhanh mat vinh vien phu cap thap). Danh sach -64 xep gan cap nhat truoc, toi da 20;
 * So tay -> Nhan nhiem vu phu: day du, chia trang "Xem thêm". He cu (tat sv.lau_dai) khong doi.
 */
public final class KiemPhuMoRong {

    private KiemPhuMoRong() {
    }

    static InfoQuestTemplate mau(final short id, final int lv) {
        final InfoQuestTemplate t = new InfoQuestTemplate();
        t.id = id;
        t.type = 0;
        t.lv = (short) lv;
        t.ma = "P-98." + id;
        t.name = "Phụ thử " + lv;
        t.main_sub = 1;
        t.totalMonsKilled.add((short) 5);
        return t;
    }

    public static void chay() {
        Kiem.nhom("Nhiem vu phu mo rong: khong chan duoi, xep gan cap, toi da 20, trang So tay");

        // ---- luat cap
        Kiem.dung("cap 40 thay phu cap 15", NhiemVuLauDai.hopCap(15, 40));
        Kiem.dung("cap 40 thay phu cap 1", NhiemVuLauDai.hopCap(1, 40));
        Kiem.dung("cap 40 thay phu cap 50", NhiemVuLauDai.hopCap(50, 40));
        Kiem.sai("cap 40 khong thay phu cap 51", NhiemVuLauDai.hopCap(51, 40));

        // ---- thu tu + tran
        final int[] lv = {15, 38, 45, 51, 39, 41, 1};
        final List<Integer> chon = NhiemVuLauDai.locPhu(lv, 40, NhiemVuLauDai.PHU_TOI_DA);
        final List<Integer> capChon = new ArrayList<Integer>();
        for (final Integer i : chon) {
            capChon.add(Integer.valueOf(lv[i.intValue()]));
        }
        // chu game 31/10/2026: danh sach NPC chi cap 25..43 (cap - 15 .. cap + 3); 45 / 51 (truoc) va 15 / 1 (qua xa) o So tay
        Kiem.bang("xep gan cap 40 nhat truoc (cung lech: thap truoc), chi cap 25..43", "[39, 41, 38]", capChon.toString());
        final int[] nhieu = new int[300];
        for (int i = 0; i < nhieu.length; i++) {
            nhieu[i] = 1 + i % 60;
        }
        final List<Integer> c2 = NhiemVuLauDai.locPhu(nhieu, 40, NhiemVuLauDai.PHU_TOI_DA);
        Kiem.bang("300 phu (5 / cap 1..60) -> gui het cua so cap 25..43 (95)", Integer.valueOf(95), Integer.valueOf(c2.size()));
        Kiem.bang("muc dau la phu cap 40", Integer.valueOf(40), Integer.valueOf(nhieu[c2.get(0).intValue()]));

        // ---- trang So tay
        Kiem.bang("20 muc: 1 trang, khong Xem them", "[0, 20, 1, 0]", java.util.Arrays.toString(NhiemVuEngine.trangPhu(20, 0)));
        Kiem.bang("45 muc trang 1: 19 muc + Xem them", "[0, 19, 3, 0]", java.util.Arrays.toString(NhiemVuEngine.trangPhu(45, 0)));
        Kiem.bang("45 muc trang 3", "[38, 45, 3, 2]", java.util.Arrays.toString(NhiemVuEngine.trangPhu(45, 2)));
        Kiem.bang("qua trang cuoi -> ve trang 1", "[0, 19, 3, 0]", java.util.Arrays.toString(NhiemVuEngine.trangPhu(45, 3)));
        Kiem.dung("trang + Xem them van <= DS_TOI_DA", NhiemVuMau.DS_TOI_DA - 1 + 1 <= NhiemVuMau.DS_TOI_DA);

        // ---- So tay: nhan phu cap 15 o cap 40 / cap 50 truoc 10 cap / 51 thi khong
        final NhiemVuMau m = new NhiemVuMau();
        m.loai = "phu";
        m.cap = 15;
        Kiem.dung("So tay: cap 40 nhan duoc phu cap 15", m.kiemNhan(40, 0, new java.util.HashSet<String>(), true) == null);
        m.cap = 50;
        Kiem.dung("So tay: cap 40 nhan duoc phu cap 50", m.kiemNhan(40, 0, new java.util.HashSet<String>(), true) == null);
        m.cap = 51;
        Kiem.sai("So tay: cap 40 khong nhan phu cap 51", m.kiemNhan(40, 0, new java.util.HashSet<String>(), true) == null);
        m.loai = "chinh";
        m.cap = 41;
        Kiem.sai("chinh van can du cap", m.kiemNhan(40, 0, new java.util.HashSet<String>(), true) == null);

        // ---- danh sach -64 that (NhiemVuLauDaiGame.locDanhSach) + he cu
        final boolean cu = LauDai.bat();
        final Hashtable<Short, InfoQuestTemplate> phuCu = new Hashtable<Short, InfoQuestTemplate>(Map.allSubQuest);
        final real.GemTemplate[] gemCu = Map.gemTemplate;
        if (Map.gemTemplate == null) {
            Map.gemTemplate = new real.GemTemplate[0];
        }
        try {
            Map.allSubQuest.clear();
            Map.allSubQuest.put((short) 31701, mau((short) 31701, 15));
            Map.allSubQuest.put((short) 31702, mau((short) 31702, 39));
            Map.allSubQuest.put((short) 31703, mau((short) 31703, 51));
            final Char p = new Char(null);
            p.lvDetail.lv = 40;
            p.mainQuest = new NewCharQuest(true);
            p.mainQuest.state_quest = NewCharQuest.DONE;
            p.subQuest = new NewCharQuest(true);
            LauDai.dat(true);
            final Vector<NewCharQuest> ds = NhiemVuLauDaiGame.locDanhSach(p);
            final List<Short> id = new ArrayList<Short>();
            for (final NewCharQuest q : ds) {
                if (q.main_sub == 1) {
                    id.add(Short.valueOf(q.id_quest));
                }
            }
            // chu game 31/10/2026: NPC chi cap 25..43 -> 15 (vuot 25 cap) nhan o So tay (kiem o tren), 51 khong
            Kiem.bang("cap 40: -64 moi phu 39, khong 15 (So tay) / 51", "[31702]", id.toString());
            Kiem.sai("cap 40: phu cap 15 khong o danh sach NPC (vuot qua 15 cap)", id.contains(Short.valueOf((short) 31701)));

            LauDai.dat(false);
            final Vector<NewCharQuest> dsCu = p.getAllQuestCanReceive();
            int soPhu = 0;
            for (final NewCharQuest q : dsCu) {
                if (q.main_sub == 1) {
                    soPhu++;
                }
            }
            Kiem.bang("he cu: moi phu (khong loc cap) nhu ban goc", Integer.valueOf(3), Integer.valueOf(soPhu));
        } catch (final Exception e) {
            Kiem.dung("khong ngoai le: " + e, false);
        } finally {
            LauDai.dat(cu);
            Map.allSubQuest.clear();
            Map.allSubQuest.putAll(phuCu);
            Map.gemTemplate = gemCu;
        }
    }
}
