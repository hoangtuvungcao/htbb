package real;

import com.htbb.pokemon.HuongDanTanThu;
import com.htbb.pokemon.LauDai;
import com.htbb.thongbao.ThongBao;
import data.Database;
import data.DatabaseLauDai;
import data.InfoQuestTemplate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * HUONG DAN TAN THU - noi game (luat thuan o com.htbb.pokemon.HuongDanTanThu). Moi chu nguoi choi thay nam o bang
 * data_huong_dan_tan_thu (sql/nang_cap.sql (khoi them_huong_dan_tan_thu.sql)), nap lai moi 60 giay; 10 buoc la 10 dong dau chuoi chinh
 * (data_quest_moi 901..910) nen chay tren client 0.9.7 bang goi nhiem vu -64 san co (dau tren dau NPC, cham ban do nho,
 * nut "Di toi"), khong goi tin moi:
 *
 * <pre>
 *  suKien (NhiemVuEngine.suKien, MOT diem goi)  buoc hanh dong (kieu 2): dung_thuoc / hoc_chieu / nang_cap / tha_cau...
 *                                                 khop kieu_muc_tieu cua buoc dang lam -> +1 dau tap -> Char.checkFinishQuest
 *                                               "den_ban_do" o lang, buoc 1 chua nhan -> menu Lam / Bo qua (mot lan / phien)
 *  khiNhan (Char nhan nhiem vu chinh)           dieu kien da dat san (dang mac Ruy bang, he sinh thai tat) -> xong ngay
 *  khiNap (LauDaiNhanVat.khiNap)                buoc huong dan dang lam luu theo du lieu cu (mang tien do lech mau) -> lam lai buoc
 *  So tay (-527): muc Bo qua huong dan / Huong dan tan thu (cong dung tung NPC, Pokemon cap thap)
 *  boQua (popup -32 id 96, co xac nhan)        nhay toi buoc dau sau huong dan, PHAT DU thuong cac buoc con lai
 * </pre>
 */
public final class HuongDanTanThuGame {

    private static volatile java.util.Map<String, String[]> CHU = Collections.emptyMap();
    /** ma HD.xx -> {kieu_muc_tieu, muc_tieu_id} (data_quest_lau_dai; engine nhiem vu khong nap dong huong_dan). */
    private static volatile java.util.Map<String, String[]> KIEU = Collections.emptyMap();
    private static volatile long napLuc;
    /** charDBID da duoc moi Lam / Bo qua trong phien may chu nay. */
    private static final Set<Integer> DA_MOI = Collections.newSetFromMap(new ConcurrentHashMap<Integer, Boolean>());

    private HuongDanTanThuGame() {
    }

    // ------------------------------------------------------------------ chu (CSDL)

    static void napNeuCan() {
        final long bay = System.currentTimeMillis();
        if (bay - napLuc < 60000L && !CHU.isEmpty()) {
            return;
        }
        napLuc = bay;
        final java.util.Map<String, String[]> m = DatabaseLauDai.napHuongDanTanThu();
        if (!m.isEmpty()) {
            CHU = m;
        }
        final java.util.Map<String, String[]> k = DatabaseLauDai.napKieuHuongDan();
        if (!k.isEmpty()) {
            KIEU = k;
        }
    }

    /** Noi dung theo ma ("" neu thieu - khi do khong gui gi). */
    public static String chu(final String ma) {
        napNeuCan();
        final String[] d = CHU.get(ma);
        return d == null || d[1] == null ? "" : d[1];
    }

    /** Nhan ngan (cot tieu_de), roi ve noi dung neu trong. */
    static String nhan(final String ma) {
        napNeuCan();
        final String[] d = CHU.get(ma);
        if (d == null) {
            return "";
        }
        return d[0] != null && !d[0].isEmpty() ? d[0] : (d[1] == null ? "" : d[1]);
    }

    /** Cac dong cong dung NPC (ma "npc.*", theo thu_tu): {ma, tieu_de, noi_dung}. */
    static List<String[]> dongNpc() {
        napNeuCan();
        final List<String[]> kq = new ArrayList<String[]>();
        for (final Entry<String, String[]> e : CHU.entrySet()) {
            if (e.getKey().startsWith("npc.")) {
                kq.add(new String[]{e.getKey(), e.getValue()[0], e.getValue()[1]});
            }
        }
        return kq; // CHU giu thu tu thu_tu (LinkedHashMap)
    }

    // ------------------------------------------------------------------ buoc

    /** So buoc huong dan = so dong "HD." lien tiep o dau chuoi chinh (Map.allMainQuest danh so 0,1,2.. theo id). */
    public static int soBuoc() {
        int n = 0;
        while (true) {
            final InfoQuestTemplate t = Map.allMainQuest.get((short) n);
            if (t == null || !HuongDanTanThu.laMa(t.ma)) {
                return n;
            }
            n++;
        }
    }

    /** {kieu_muc_tieu, muc_tieu_id} cua buoc huong dan, null neu khong phai. */
    static String[] mau(final InfoQuestTemplate info) {
        if (info == null || !HuongDanTanThu.laMa(info.ma)) {
            return null;
        }
        napNeuCan();
        return KIEU.get(info.ma);
    }

    // ------------------------------------------------------------------ su kien

    /** NhiemVuEngine.suKien (moi su kien game di qua day mot lan). */
    public static void suKien(final Char p, final String kieu, final String thamSo, final int n) {
        if (p == null || p.charDBID <= 0 || n <= 0 || !LauDai.bat() || kieu == null) {
            return;
        }
        try {
            if ("den_ban_do".equals(kieu)) {
                moiNeuCan(p);
            } else if (HuongDanTanThu.laKieuHanhDong(kieu)) {
                tienDo(p, kieu, thamSo);
            }
        } catch (final Exception e) {
            System.out.println("HuongDanTanThuGame.suKien " + kieu + ": " + e);
        }
    }

    /** Buoc hanh dong (kieu 2, ma HD.) dang lam khop su kien -> +1 dau tap; du thi Char.checkFinishQuest bao "di tra". */
    static boolean tienDo(final Char p, final String kieu, final String thamSo) {
        final NewCharQuest q = p.mainQuest;
        if (q == null || q.state_quest != NewCharQuest.WORKING) {
            return false;
        }
        final InfoQuestTemplate info = q.getTemplate();
        if (info == null || info.type != 2 || !HuongDanTanThu.laMa(info.ma) || info.itemget.isEmpty()) {
            return false;
        }
        final String[] m = mau(info);
        if (m == null || !HuongDanTanThu.khop(m[0], m[1], kieu, thamSo)) {
            return false;
        }
        congDau(p, q, info);
        return true;
    }

    static void congDau(final Char p, final NewCharQuest q, final InfoQuestTemplate info) {
        final short id = info.itemget.get(0).shortValue();
        if (id < 0 || id >= p.itemquest.length) {
            return;
        }
        p.itemquest[id]++;
        p.sendMessage(MessageCreator.createCharInventoryMessage(p, 3));
        p.checkFinishQuest(id, false, 0);
        if (q.state_quest == NewCharQuest.WORKING) {
            MessageCreator.createMsgCharQuest(p, 2); // tien do x/y (checkFinishQuest chi gui khi xong)
        }
    }

    /** Char.xuLyNhiemVu: vua nhan nhiem vu chinh - buoc hanh dong da dat san thi tinh luon. */
    public static void khiNhan(final Char p, final InfoQuestTemplate info) {
        try {
            if (p == null || info == null || info.type != 2 || !HuongDanTanThu.laMa(info.ma)) {
                return;
            }
            final String[] m = mau(info);
            if (m == null || p.mainQuest == null || p.mainQuest.state_quest != NewCharQuest.WORKING) {
                return;
            }
            boolean daDat = false;
            if ("nang_cap".equals(m[0])) {
                final java.util.Vector<Item> mac = p.getCharWearing();
                for (int i = 0; mac != null && i < mac.size() && !daDat; i++) {
                    final Item it = mac.get(i);
                    daDat = it != null && it.getTemplate() != null
                            && HuongDanTanThu.khop(m[0], m[1], m[0], String.valueOf(it.getTemplate().id));
                }
            } else if ("hoc_chieu".equals(m[0]) && p.skillpoint <= 0) {
                // da cong het diem truoc khi nhan buoc nay (khong con diem de cong): da biet cach, khong bat cho len cap
                int daCong = 0;
                for (int i = 0; p.skill != null && i < p.skill.length; i++) {
                    daCong += Math.max(0, p.skill[i]);
                }
                daDat = daCong > 0;
            } else if ("tha_cau".equals(m[0]) || "cau_ca".equals(m[0])) {
                daDat = !com.htbb.pokemon.HeSinhThai.bat(); // khong co he sinh thai thi khong cau duoc: khong chan nguoi choi
            }
            if (daDat) {
                congDau(p, p.mainQuest, info);
            }
        } catch (final Exception e) {
            System.out.println("HuongDanTanThuGame.khiNhan: " + e);
        }
    }

    /**
     * LauDaiNhanVat.khiNap: buoc huong dan dang lam / xong-chua-tra ma mang tien do luu lech mau hien tai (du lieu buoc
     * da doi, vd ban cu "ha 20 quai" nay la "cong diem ky nang") -> nhan lai buoc do tu dau. Khong doi chi so chuoi.
     */
    public static void khiNap(final Char p) {
        try {
            final int sb = soBuoc();
            if (p == null || p.mainQuest == null || !HuongDanTanThu.dangHuongDan(p.idMainQuest, sb)
                    || p.mainQuest.state_quest == NewCharQuest.UN_RECEIVE || p.mainQuest.state_quest == NewCharQuest.DONE) {
                return;
            }
            final InfoQuestTemplate t = p.mainQuest.getTemplate();
            if (t == null) {
                return;
            }
            final NewCharQuest q = p.mainQuest;
            final boolean lech = q.nItem == null || q.monsterKilled == null || q.nItem.length != t.totalitemget.size()
                    || q.monsterKilled.length != t.totalMonsKilled.size();
            if (lech) {
                p.mainQuest = new NewCharQuest(p.idMainQuest, (byte) 0);
                Database.instance.saveCharQuest(p.charDBID, p.getInfoQuestSave());
            }
        } catch (final Exception e) {
            System.out.println("HuongDanTanThuGame.khiNap: " + e);
        }
    }

    // ------------------------------------------------------------------ moi khi vao lang

    static void moiNeuCan(final Char p) {
        if (p.mainQuest == null) {
            return;
        }
        final boolean da = DA_MOI.contains(Integer.valueOf(p.charDBID));
        if (!HuongDanTanThu.nenMoi(p.idMainQuest, p.mainQuest.state_quest, NpcTinhBanDo.banDoNap(p), soBuoc(), da)) {
            return;
        }
        final String lam = nhan("moi.lam");
        final String bo = nhan("moi.bo_qua");
        if (lam.isEmpty() || bo.isEmpty()) {
            return; // chua chay sql/nang_cap.sql (khoi them_huong_dan_tan_thu.sql)
        }
        DA_MOI.add(Integer.valueOf(p.charDBID));
        ThongBao.gui(p, ThongBao.TIN, chu("moi.chao"), chu("moi.chao_phu"), ThongBao.HINH_KHONG, 0);
        p.sendMessage(MessageCreator.createMsgMenu(new String[]{lam, bo}, SoTayLauDai.NPC_ID, HuongDanTanThu.MENU_MOI));
    }

    // ------------------------------------------------------------------ So tay

    /** Muc noi them vao cuoi menu goc So tay (theo trang thai nhan vat), {ma muc}. */
    static List<String> mucSoTay(final Char p) {
        final List<String> kq = new ArrayList<String>();
        final int sb = soBuoc();
        if (HuongDanTanThu.dangHuongDan(p.idMainQuest, sb) && !nhan("so_tay.bo_qua").isEmpty()) {
            kq.add("so_tay.bo_qua");
        }
        if (HuongDanTanThu.coMucXemLai(p.lvDetail == null ? 999 : p.lvDetail.lv, p.idMainQuest, sb)
                && !nhan("so_tay.huong_dan").isEmpty()) {
            kq.add("so_tay.huong_dan");
        }
        return kq;
    }

    /** SoTayLauDai.moMenu: menu goc + muc huong dan. */
    public static String[] themMucSoTay(final Char p, final String[] goc) {
        try {
            final List<String> them = mucSoTay(p);
            final String[] kq = java.util.Arrays.copyOf(goc, goc.length + them.size());
            for (int i = 0; i < them.size(); i++) {
                kq[goc.length + i] = nhan(them.get(i));
            }
            return kq;
        } catch (final Exception e) {
            return goc;
        }
    }

    /** SoTayLauDai.doMenu menu goc, muc thu {@code i} sau cac muc co dinh. */
    public static void chonMucSoTay(final Char p, final int i) {
        final List<String> them = mucSoTay(p);
        if (i < 0 || i >= them.size()) {
            return;
        }
        if ("so_tay.bo_qua".equals(them.get(i))) {
            hoiBoQua(p);
        } else {
            moDanhSachNpc(p);
        }
    }

    static void moDanhSachNpc(final Char p) {
        final List<String[]> d = dongNpc();
        if (d.isEmpty()) {
            return;
        }
        final String[] menu = new String[d.size()];
        for (int i = 0; i < menu.length; i++) {
            menu[i] = d.get(i)[1];
        }
        p.sendMessage(MessageCreator.createMsgMenu(menu, SoTayLauDai.NPC_ID, HuongDanTanThu.MENU_NPC));
    }

    /** SoTayLauDai.doMenu: menu MENU_MOI / MENU_NPC. */
    public static void doMenu(final Char p, final int idMenu, final int chon) {
        if (idMenu == HuongDanTanThu.MENU_MOI) {
            if (chon == 1) {
                hoiBoQua(p);
            } else if (chon == 0) {
                ThongBao.gui(p, ThongBao.TIN, chu("moi.bat_dau"), "", ThongBao.HINH_KHONG, 0);
            }
        } else if (idMenu == HuongDanTanThu.MENU_NPC) {
            final List<String[]> d = dongNpc();
            if (chon >= 0 && chon < d.size()) {
                p.sendMessage(MessageCreator.createServerAlertMessage(d.get(chon)[2], ""));
            }
        }
    }

    // ------------------------------------------------------------------ bo qua

    static void hoiBoQua(final Char p) {
        final int sb = soBuoc();
        if (!HuongDanTanThu.dangHuongDan(p.idMainQuest, sb)) {
            ThongBao.gui(p, ThongBao.TIN, chu("bo_qua.da_xong"), "", ThongBao.HINH_KHONG, 0);
            return;
        }
        final String hoi = HuongDanTanThu.thay(chu("bo_qua.hoi"), "thuong", tomTatThuong(p.idMainQuest, sb));
        if (hoi.isEmpty()) {
            return;
        }
        p.sendMessage(MessageCreator.createMsgPopUp(p.id, HuongDanTanThu.POPUP_BO_QUA, hoi));
    }

    /** Map.doPopup id 96. */
    public static void khiXacNhan(final Char p, final boolean dongY) {
        if (p == null || !dongY) {
            return;
        }
        synchronized (p.khoaNhiemVu()) {
            boQua(p);
        }
    }

    /** Tong thuong cac buoc con lai ("3.512 EXP, 900 xu, 6 <ten thuoc>, <ten do>" - ten lay tu CSDL). */
    static String tomTatThuong(final int idChinh, final int sb) {
        long exp = 0;
        long xu = 0;
        final java.util.TreeMap<Integer, Integer> thuoc = new java.util.TreeMap<Integer, Integer>();
        final List<String> doVat = new ArrayList<String>();
        for (final int i : HuongDanTanThu.buocConLai(idChinh, sb)) {
            final InfoQuestTemplate t = Map.allMainQuest.get((short) i);
            if (t == null) {
                continue;
            }
            exp += t.exp;
            xu += t.gold;
            if (t.potiongift != null && t.potiongift.length > 0 && t.potiongift[0] < 32000) {
                for (int k = 0; k < t.potiongift.length && k < t.npotiongift.length; k++) {
                    thuoc.merge(Integer.valueOf(t.potiongift[k]), Integer.valueOf(t.npotiongift[k]), Integer::sum);
                }
            }
            if (Char.canOTrongDeTra(t.idItemgift)) {
                try {
                    doVat.add(Map.itemTemplates.get(0).get((int) t.idItemgift[0]).name);
                } catch (final Exception ignored) {
                    // template thieu: chi bo ten khoi tom tat, thuong van phat
                }
            }
        }
        final StringBuilder b = new StringBuilder(HuongDanTanThu.so(exp)).append(" EXP, ").append(HuongDanTanThu.so(xu)).append(" xu");
        for (final Entry<Integer, Integer> e : thuoc.entrySet()) {
            String ten = "";
            try {
                ten = Map.potionTemplates2.get(e.getKey()).name;
            } catch (final Exception ignored) {
            }
            b.append(", ").append(e.getValue()).append(' ').append(ten);
        }
        for (final String d : doVat) {
            b.append(", ").append(d);
        }
        return b.toString();
    }

    /** Thuc hien bo qua (da khoa nhiem vu). Tra ve true neu da bo qua. */
    static boolean boQua(final Char p) {
        final int sb = soBuoc();
        final int id = p.idMainQuest;
        if (!HuongDanTanThu.dangHuongDan(id, sb)) {
            ThongBao.gui(p, ThongBao.TIN, chu("bo_qua.da_xong"), "", ThongBao.HINH_KHONG, 0);
            p.sendInfoQuest();
            return false;
        }
        final int[] con = HuongDanTanThu.buocConLai(id, sb);
        int canO = 0;
        final Set<Integer> thuocMoi = new java.util.HashSet<Integer>();
        for (final int i : con) {
            final InfoQuestTemplate t = Map.allMainQuest.get((short) i);
            if (t == null) {
                continue;
            }
            if (Char.canOTrongDeTra(t.idItemgift)) {
                canO++;
            }
            if (t.potiongift != null && t.potiongift.length > 0 && t.potiongift[0] < 32000) {
                for (final short th : t.potiongift) {
                    if (th > 0 && th < p.potions.length && p.potions[th] <= 0) {
                        thuocMoi.add(Integer.valueOf(th));
                    }
                }
            }
        }
        if (canO + thuocMoi.size() > p.soOTrongHanhTrang()) {
            ThongBao.gui(p, ThongBao.LOI, chu("bo_qua.tui_day"), "", ThongBao.HINH_KHONG, 0);
            return false;
        }
        final String tomTat = tomTatThuong(id, sb);
        for (final int i : con) {
            final InfoQuestTemplate t = Map.allMainQuest.get((short) i);
            if (t == null) {
                continue;
            }
            t.addGift(p);
            NhiemVuEngine.khiXongChuoiCu(p, t.ma);
            if (t.type == 1 || t.type == 2) {
                for (final Short v : t.itemget) {
                    if (v != null && v.shortValue() >= 0 && v.shortValue() < p.itemquest.length) {
                        p.itemquest[v.shortValue()] = 0; // dau tap / thu cua buoc huong dan
                    }
                }
            }
        }
        p.idMainQuest = (short) sb;
        p.mainQuest = new NewCharQuest((short) sb, (byte) 0);
        p.sendMessage(MessageCreator.createCharInventoryMessage(p, 3));
        p.sendInfoQuest();
        Database.instance.saveCharQuest(p.charDBID, p.getInfoQuestSave());
        Database.instance.saveOrtherLog("", p.charname, "bo qua huong dan tan thu " + id + " -> " + sb + ": " + tomTat, "huongdan");
        ThongBao.gui(p, ThongBao.KET_QUA, HuongDanTanThu.thay(chu("bo_qua.xong"), "thuong", tomTat), "", ThongBao.HINH_KHONG, 0);
        return true;
    }
}
