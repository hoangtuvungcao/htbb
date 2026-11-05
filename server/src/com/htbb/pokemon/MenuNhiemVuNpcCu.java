package com.htbb.pokemon;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Menu nhiem vu NPC cho CLIENT CU (ban &lt; {@link #BAN_CLIENT}) - bao loi nguoi choi 05/11/2026 (Truong lang Xatu: luoi 7
 * ten nhiem vu + "Thêm", "khong biet cai nao la nhan, tra, nhet nhieu qua").
 *
 * Client tu dung menu NPC tu danh sach -64 loai 0 (GameScr.getAllQuestNpc): ten tron, moi nhiem vu phu cua NPC trai het ra.
 * Client &gt;= 932 tu gan nhan / xep / gom (game.model.MenuNhiemVuNpc). Client cu: may chu
 *   - gan tien to "! Nhận: " vao ten nhiem vu nhan duoc (cung chu voi client moi),
 *   - xep buoc chinh truoc,
 *   - moi NPC gui toi da {@link #PHU_MOI_NPC} nhiem vu phu (gan cap nhat - thu tu goc da xep gan cap). Mot luc chi giu MOT
 *     nhiem vu phu NPC (khe subQuest) nen 3 lua chon la du; phan con lai van o So tay -> Nhan nhiem vu phu.
 * Chi doi GOI gui xuong; danh sach nhan hop le (Char.getAllQuestCanReceive) khong doi.
 */
public final class MenuNhiemVuNpcCu {
    /** Ban client tu dung menu nhiem vu NPC co nhan (0.9.32). */
    public static final int BAN_CLIENT = 932;
    /** Trung voi game.model.MenuNhiemVuNpc.NHAN (KiemMenuNhiemVuNpc so hai ben). */
    public static final String NHAN = "! Nhận: ";
    public static final int PHU_MOI_NPC = 3;

    private MenuNhiemVuNpcCu() {
    }

    /** Client nay tu gan nhan menu NPC chua (Unity / khong gui so ban = -1: coi la cu). */
    public static boolean clientTuGan(final int banClient) {
        return banClient >= BAN_CLIENT;
    }

    /** Ten nhiem vu nhan duoc cho client cu: "! Nhận: tên" (khong lap tien to). */
    public static String tenNhan(final String ten) {
        final String t = ten == null ? "" : ten;
        return t.startsWith(NHAN) ? t : NHAN + t;
    }

    /**
     * Chon va xep danh sach nhan duoc gui client cu.
     * @param npc NPC giao cua tung dong
     * @param mainSub 0 chinh, 1 phu, 2 doi
     * @return chi so dong giu lai: moi dong chinh (theo thu tu goc) truoc, roi phu / doi theo thu tu goc, moi NPC toi da
     *         PHU_MOI_NPC dong phu (doi khong gioi han - NPC doi rieng)
     */
    public static List<Integer> loc(final int[] npc, final int[] mainSub) {
        final List<Integer> kq = new ArrayList<Integer>();
        for (int i = 0; i < mainSub.length; i++) {
            if (mainSub[i] == 0) {
                kq.add(Integer.valueOf(i));
            }
        }
        final HashMap<Integer, Integer> dem = new HashMap<Integer, Integer>();
        for (int i = 0; i < mainSub.length; i++) {
            if (mainSub[i] == 0) {
                continue;
            }
            if (mainSub[i] == 1) {
                final Integer k = Integer.valueOf(npc[i]);
                final Integer n = dem.get(k);
                final int c = n == null ? 0 : n.intValue();
                if (c >= PHU_MOI_NPC) {
                    continue;
                }
                dem.put(k, Integer.valueOf(c + 1));
            }
            kq.add(Integer.valueOf(i));
        }
        return kq;
    }
}
