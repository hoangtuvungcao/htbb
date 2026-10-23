package real;

import com.htbb.pokemon.KhoaBanDo;
import com.htbb.pokemon.LauDai;
import com.htbb.pokemon.MucTieuNhiemVu;
import com.htbb.pokemon.NhiemVuMau;
import data.InfoQuestTemplate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Muc tieu nhiem vu trong so Nhiem vu (nhanh sua-tien-do-nv). Nguoi choi (0.9.13) bao: "nhận nhiệm vụ rồi mà không xem
 * được tiến độ", "đánh 8 chục con vẫn chưa báo hoàn thành" - hang so Nhiem vu chi co ten + loai + x/N, chu "Hạ 20 Weedle
 * ở Pewter" sai ban do (Weedle chi o Lavender, ban do khoa sau cua ai), cua ai dang lam khong hien trong so.
 *
 * <ul>
 *   <li>{@link #cua}: muc tieu / tra / ly do khoa cua mot nhiem vu cu (-64) sinh tu du lieu DEM (mau quai, so can) va ban
 *       do quai that su xuat hien ({@link #banDoQuai}: chi muc sinh quai tu RealController.mapList), khong tu scontent.</li>
 *   <li>MessageCreator.createMsgCharQuest: client &lt; {@link MucTieuNhiemVu#BAN_CLIENT} -> ten + " · Weedle ở Lavender",
 *       mo ta chi tiet "Mục tiêu: … · Trả: …"; client &gt;= 915 -> ten goc + duoi goi (muc / tra / khoa / co).</li>
 *   <li>Cua ai dang lam (NhiemVuEngine, ma CA-) hien o KHE DOI (main_sub 2) khi nhan vat khong co nhiem vu doi:
 *       {@link #cuaAiTrongSo}; id &gt;= {@link #ID_CUA_AI}; goi -64 cua client cho id do -> {@link #khiGoiCuaAi}.</li>
 *   <li>Ha quai muc tieu cua mot buoc cua ai chua nhan -> hop 95 MOT lan / buoc / phien ({@link #khiHaQuai}).</li>
 * </ul>
 */
public final class MucTieuNhiemVuGame {

    /** Id nhiem vu ao cua cua ai trong khe doi: 30000 + chuong * 10 + buoc (CA-01.2 -> 30012). */
    public static final int ID_CUA_AI = 30000;

    private static volatile HashMap<Integer, TreeSet<Integer>> QUAI_O;
    private static volatile int soMapLucNap = -1;
    /** charDBID -> buoc cua ai da nhac (khong nhac lai trong phien). */
    private static final ConcurrentHashMap<Integer, java.util.Set<String>> DA_NHAC = new ConcurrentHashMap<Integer, java.util.Set<String>>();

    private MucTieuNhiemVuGame() {
    }

    /** Ket qua cho mot nhiem vu. */
    public static final class KetQua {
        public String muc = "";
        public String ngan = "";
        public String tra = "";
        public String khoa = "";
        public int co;
    }

    // ------------------------------------------------------------------ ban do quai

    /** Test: dat bang mau quai -> ban do nap. */
    public static void datBangQuai(final HashMap<Integer, TreeSet<Integer>> m) {
        QUAI_O = m;
        soMapLucNap = Integer.MAX_VALUE;
    }

    /** Mau quai -> cac ban do nap (mapIDLoadMap) co quai nay luc sinh. Nap lai khi so ban do doi. */
    public static TreeSet<Integer> banDoQuai(final int mau) {
        HashMap<Integer, TreeSet<Integer>> m = QUAI_O;
        final int n = RealController.mapList == null ? 0 : RealController.mapList.size();
        if (m == null || soMapLucNap != Integer.MAX_VALUE && soMapLucNap != n) {
            m = napBangQuai();
            QUAI_O = m;
            soMapLucNap = n;
        }
        final TreeSet<Integer> s = m.get(Integer.valueOf(mau));
        return s == null ? new TreeSet<Integer>() : s;
    }

    static HashMap<Integer, TreeSet<Integer>> napBangQuai() {
        final HashMap<Integer, TreeSet<Integer>> kq = new HashMap<Integer, TreeSet<Integer>>();
        try {
            for (final Map map : new ArrayList<Map>(RealController.mapList.values())) {
                if (map == null || map.isMapOffline) {
                    continue;
                }
                final List<Hashtable<Short, Monster>> bang = new ArrayList<Hashtable<Short, Monster>>();
                if (map.monsters != null && map.monsters.size() > 0) {
                    bang.add(map.monsters.get(0));
                }
                if (map.ALL_REGION != null) {
                    for (final Region r : map.ALL_REGION) {
                        if (r != null && r.monsters != null && r.monsters.size() > 0) {
                            bang.add(r.monsters.get(0));
                        }
                    }
                }
                for (final Hashtable<Short, Monster> h : bang) {
                    if (h == null) {
                        continue;
                    }
                    for (final Monster mo : new ArrayList<Monster>(h.values())) {
                        if (mo == null || mo.isBoss) {
                            continue;
                        }
                        final Integer goc = NoiDungLauDai.gocNam2(mo);
                        final int mau = goc != null ? goc.intValue() : mo.idTemplate;
                        TreeSet<Integer> s = kq.get(Integer.valueOf(mau));
                        if (s == null) {
                            kq.put(Integer.valueOf(mau), s = new TreeSet<Integer>());
                        }
                        s.add(Integer.valueOf(map.mapIDLoadMap));
                    }
                }
            }
        } catch (final Exception e) {
            System.out.println("MucTieuNhiemVuGame.napBangQuai: " + e);
        }
        return kq;
    }

    static int capMo(final int map) {
        final KhoaBanDo.Luat l = KhoaBanDo.luat(map);
        return l == null ? 0 : l.cap;
    }

    static String tenMap(final int map) {
        try {
            final String t = TenBanDoMayChu.ten(map);
            return t == null ? "" : t;
        } catch (final Exception e) {
            return "";
        }
    }

    /**
     * Ban do nen di cho mau quai {@code mau}: {id, khoa (1 = moi ban do deu khoa voi nguoi choi)}; ban do dang dung neu co
     * quai, nguoc lai ban do MO co cap mo thap nhat, khong co thi ban do khoa cap mo thap nhat. null = khong ban do nao.
     */
    static int[] chonBanDo(final Char p, final int mau) {
        final TreeSet<Integer> ds = banDoQuai(mau);
        if (ds.isEmpty()) {
            return null;
        }
        final int dang = p == null ? -1 : NpcTinhBanDo.banDoNap(p);
        if (ds.contains(Integer.valueOf(dang))) {
            return new int[]{dang, 0};
        }
        int moTot = -1, khoaTot = -1;
        for (final Integer id : ds) {
            final boolean mo = p == null || KhoaBanDoGame.lyDoChan(p, id.intValue()) == null;
            if (mo && (moTot < 0 || capMo(id.intValue()) < capMo(moTot))) {
                moTot = id.intValue();
            }
            if (!mo && (khoaTot < 0 || capMo(id.intValue()) < capMo(khoaTot))) {
                khoaTot = id.intValue();
            }
        }
        return moTot >= 0 ? new int[]{moTot, 0} : new int[]{khoaTot, 1};
    }

    // ------------------------------------------------------------------ NPC tra

    /** "Machamp (Pewter)": NPC tinh loai {@code loai} - o ban do dang dung, lang ve cua nhan vat, hoac ban do dau tien. */
    static String noiNpc(final Char p, final int loai) {
        if (loai < 0 || loai >= 32000) {
            return "";
        }
        final String ten = MucTieuNhiemVu.tenNgan(com.htbb.pokemon.ChuPokemon.tenNpc(loai, ""));
        if (ten.isEmpty()) {
            return "";
        }
        List<Integer> co = Collections.emptyList();
        try {
            co = NpcTinhBanDo.banDoCo(loai);
        } catch (final Exception ignored) {
        }
        int noi = -1;
        final int dang = p == null ? -1 : NpcTinhBanDo.banDoNap(p);
        if (co.contains(Integer.valueOf(dang))) {
            noi = dang;
        } else {
            int lang = -1;
            try {
                lang = p == null ? -1 : PhongRiengGame.langVe(p);
            } catch (final Exception ignored) {
            }
            if (co.contains(Integer.valueOf(lang))) {
                noi = lang;
            } else {
                for (final int l : new int[]{0, 70, 80}) {
                    if (co.contains(Integer.valueOf(l))) {
                        noi = l;
                        break;
                    }
                }
                if (noi < 0 && !co.isEmpty()) {
                    noi = co.get(0).intValue();
                }
            }
        }
        return MucTieuNhiemVu.tra(ten, noi < 0 ? "" : tenMap(noi));
    }

    // ------------------------------------------------------------------ nhiem vu cu (-64)

    static String tenQuai(final int mau) {
        final MonsterTemplate t = Map.monsterTemplates.get(Integer.valueOf(mau));
        return t == null || t.name == null ? "" : t.name;
    }

    static String tenVatPham(final int id) {
        final String[] ds = data.ItemQuest.NAME_ITEM_QUEST;
        return ds != null && id >= 0 && id < ds.length && ds[id] != null ? ds[id] : "";
    }

    /** Muc tieu / tra / khoa cua nhiem vu {@code info} voi nguoi choi {@code p} ({@code nhom} -64: 0 nhan, 1 xong, 2 dang lam). */
    public static KetQua cua(final Char p, final InfoQuestTemplate info, final int nhom) {
        final KetQua k = new KetQua();
        if (info == null) {
            return k;
        }
        try {
            final List<MucTieuNhiemVu.Muc> ds = new ArrayList<MucTieuNhiemVu.Muc>();
            String khoa = "";
            final int n = info.monsterKill == null ? 0 : info.monsterKill.size();
            if (info.type == MucTieuNhiemVu.GIET || info.type == MucTieuNhiemVu.THU) {
                for (int i = 0; i < n; i++) {
                    final int mau = info.monsterKill.get(i).intValue();
                    if (mau <= 0 || mau >= 32000) {
                        continue;
                    }
                    final int can;
                    String vp = null;
                    if (info.type == MucTieuNhiemVu.GIET) {
                        can = i < info.totalMonsKilled.size() ? info.totalMonsKilled.get(i).intValue() : 0;
                    } else {
                        can = i < info.totalitemget.size() ? info.totalitemget.get(i).intValue() : 0;
                        vp = i < info.itemget.size() ? tenVatPham(info.itemget.get(i).intValue()) : "";
                    }
                    final int[] bd = chonBanDo(p, mau);
                    if (bd != null && bd[1] == 1 && khoa.isEmpty() && nhom != 1) {
                        final String ly = p == null ? null : KhoaBanDoGame.lyDoChan(p, bd[0]);
                        khoa = ly == null ? "" : ly;
                    }
                    ds.add(new MucTieuNhiemVu.Muc(tenQuai(mau), can, vp, bd == null ? "" : tenMap(bd[0])));
                }
            } else if (info.type == MucTieuNhiemVu.GIET_CAP) {
                final int can = info.totalMonsKilled != null && info.totalMonsKilled.size() > 0 ? info.totalMonsKilled.get(0).intValue() : 0;
                ds.add(new MucTieuNhiemVu.Muc("", can, null, ""));
            }
            final MucTieuNhiemVu.Muc[] a = ds.toArray(new MucTieuNhiemVu.Muc[0]);
            k.muc = MucTieuNhiemVu.muc(info.type, a, info.deltaLv, info.lv, info.shortContent);
            k.ngan = MucTieuNhiemVu.ngan(info.type, a, info.deltaLv);
            final int npc = nhom == 0 ? info.idNpcRcv : KhuVucNhiemVuGame.npcTra(info, nhom);
            final int npcThat = npc >= 0 && npc < 32000 ? npc : (info.idNpcRcv >= 0 && info.idNpcRcv < 32000 && nhom != 2 ? info.idNpcRcv : -1);
            k.tra = noiNpc(p, npcThat);
            k.khoa = khoa;
            k.co = khoa.isEmpty() ? 0 : MucTieuNhiemVu.CO_KHOA;
        } catch (final Exception e) {
            System.out.println("MucTieuNhiemVuGame.cua: " + e);
            k.muc = info.shortContent == null ? "" : info.shortContent;
        }
        return k;
    }

    /**
     * "Chưa xong nhiệm vụ X…" (bam Trả o NPC khi chua du) kem muc tieu + tien do: "Mục tiêu: Hạ 20 Weedle ở Lavender
     * (3/20)." - nguoi choi 0.9.13 khong biet phai danh gi o dau. ly null / da tra -> giu nguyen.
     */
    public static String kemMucTieu(final Char p, final InfoQuestTemplate info, final NewCharQuest q, final String ly) {
        if (ly == null || q == null || q.state_quest != NewCharQuest.WORKING) {
            return ly;
        }
        try {
            final KetQua k = cua(p, info, 2);
            if (k.muc.isEmpty()) {
                return ly;
            }
            String td = "";
            if (info.type == MucTieuNhiemVu.GIET || info.type == MucTieuNhiemVu.GIET_CAP) {
                td = q.monsterKilled != null && q.monsterKilled.length > 0 ? tienDo(q.monsterKilled, info.getTotalMonsterKill()) : "";
            } else if (info.type == MucTieuNhiemVu.THU) {
                td = q.nItem != null && q.nItem.length > 0 ? tienDo(q.nItem, info.getTotalItemGet()) : "";
            }
            return ly + " Mục tiêu: " + k.muc + td + "." + (k.khoa.isEmpty() ? "" : " " + k.khoa);
        } catch (final Exception e) {
            return ly;
        }
    }

    static String tienDo(final short[] da, final short[] can) {
        int a = 0, b = 0;
        for (int i = 0; i < da.length && i < can.length; i++) {
            a += Math.min(da[i], can[i]);
            b += can[i];
        }
        return b > 0 ? " (" + a + "/" + b + ")" : "";
    }

    public static boolean clientMoi(final Char p) {
        return p != null && p.session != null && p.session.banClient >= MucTieuNhiemVu.BAN_CLIENT;
    }

    // ------------------------------------------------------------------ cua ai trong so

    /** Mot dong cua ai cho khe doi -64. */
    public static final class DongCuaAi {
        public short id;
        public String ma;
        public String ten;
        public int nhom; // 1 cho nhan thuong, 2 dang lam
        public int mau = -1; // giet: mau quai
        public int da;
        public int can;
        public String muc = "";
        public String moTa = "";
        public String khoa = "";
    }

    /** CA-01.2 -> 30012; -1 neu khong dang CA-xx.y. */
    public static int idCuaAi(final String ma) {
        try {
            if (ma == null || !ma.startsWith("CA-")) {
                return -1;
            }
            final int c = ma.indexOf('.');
            final int chuong = Integer.parseInt(ma.substring(3, c));
            final int buoc = Integer.parseInt(ma.substring(c + 1));
            return buoc >= 0 && buoc < 10 && chuong >= 0 && chuong < 270 ? ID_CUA_AI + chuong * 10 + buoc : -1;
        } catch (final RuntimeException e) {
            return -1;
        }
    }

    public static boolean laIdCuaAi(final int id) {
        return id >= ID_CUA_AI && id < ID_CUA_AI + 2700;
    }

    static String maCua(final int id) {
        final int c = (id - ID_CUA_AI) / 10;
        final int b = (id - ID_CUA_AI) % 10;
        return "CA-" + (c < 10 ? "0" : "") + c + "." + b;
    }

    /** Buoc cua ai dang lam / cho nhan thuong dau tien (theo ma), null = khong co / tat lau dai. */
    static DongCuaAi cuaAiDangLam(final Char p) {
        if (!LauDai.bat() || p == null || p.charDBID <= 0) {
            return null;
        }
        final ConcurrentHashMap<String, long[]> t = NhiemVuEngine.td(p);
        if (t == null) {
            return null;
        }
        final List<String> ma = new ArrayList<String>();
        for (final java.util.Map.Entry<String, long[]> e : t.entrySet()) {
            final NhiemVuMau m = NhiemVuEngine.MAU.get(e.getKey());
            if (m != null && m.laCuaAi() && e.getValue()[1] < 2 && idCuaAi(m.ma) > 0) {
                ma.add(m.ma);
            }
        }
        if (ma.isEmpty()) {
            return null;
        }
        Collections.sort(ma);
        final NhiemVuMau m = NhiemVuEngine.MAU.get(ma.get(0));
        final long[] v = t.get(m.ma);
        final DongCuaAi d = new DongCuaAi();
        d.id = (short) idCuaAi(m.ma);
        d.ma = m.ma;
        d.ten = m.ten;
        d.nhom = v[1] >= 1 ? 1 : 2;
        d.can = Math.max(1, m.soLuong);
        d.da = (int) Math.min(v[0], d.can);
        String noi = m.banDoChi() >= 0 ? tenMap(m.banDoChi()) : "";
        if ("giet".equals(m.kieu)) {
            try {
                d.mau = Integer.parseInt(m.mucTieuId.split("[,|]")[0].trim());
            } catch (final RuntimeException e) {
                d.mau = -1;
            }
        }
        if (d.mau > 0) {
            d.muc = "Hạ " + d.can + " " + tenQuai(d.mau) + (noi.isEmpty() ? "" : " ở " + noi);
            if (m.banDoChi() >= 0) {
                final String ly = KhoaBanDoGame.lyDoChan(p, m.banDoChi());
                d.khoa = ly == null ? "" : ly;
            }
        } else {
            d.muc = KhoaBanDo.moTaBuoc(m.mucTieuText, m.ten);
            if (!d.muc.isEmpty()) {
                d.muc = Character.toUpperCase(d.muc.charAt(0)) + d.muc.substring(1);
            }
        }
        d.moTa = (d.nhom == 1 ? "Xong! Nhận thưởng ở: " : "Mục tiêu: " + d.muc + " (" + d.da + "/" + d.can + "). Chỉ tính quái hạ ở đúng bản đồ. Nhận thưởng ở: ")
                + KhoaBanDo.DUONG + "Đang làm." + (d.khoa.isEmpty() || d.nhom == 1 ? "" : " " + d.khoa);
        return d;
    }

    /** Dong cua ai cho khe doi khi nhan vat khong co nhiem vu doi dang lam / da xong; null = khong hien. */
    public static DongCuaAi cuaAiTrongSo(final Char p, final int nhom) {
        try {
            if (p == null || p.clanQuestNew != null && p.clanQuestNew.id_quest != -1) {
                return null;
            }
            final DongCuaAi d = cuaAiDangLam(p);
            return d != null && d.nhom == nhom ? d : null;
        } catch (final Exception e) {
            System.out.println("MucTieuNhiemVuGame.cuaAiTrongSo: " + e);
            return null;
        }
    }

    /**
     * Goi -64 cua client cho id cua ai (khe doi): tra (1) -> nhan thuong neu xong / bao tien do; huy (2) -> khong huy (cua
     * ai mo ban do), bao duong So tay. Luon gui lai danh sach.
     */
    public static void khiGoiCuaAi(final Char p, final int loai, final int id) {
        try {
            final String ma = maCua(id);
            if (loai == 1) {
                NhiemVuEngine.chonDangLam(p, ma);
            } else {
                SoTayLauDai.bao(p, "Cửa ải không huỷ được (mở bản đồ). Xem ở: " + KhoaBanDo.DUONG + "Đang làm.");
            }
        } catch (final Exception e) {
            System.out.println("MucTieuNhiemVuGame.khiGoiCuaAi: " + e);
        }
        MessageCreator.createMsgCharQuest(p, 1);
        MessageCreator.createMsgCharQuest(p, 2);
    }

    /** NhiemVuEngine.suKien doi tien do mot buoc cua ai: gui lai so (khe doi dang hien cua ai). */
    static void khiDoiCuaAi(final Char p, final NhiemVuMau m) {
        if (m == null || !m.laCuaAi() || p == null || p.clanQuestNew != null && p.clanQuestNew.id_quest != -1) {
            return;
        }
        MessageCreator.createMsgCharQuest(p, 1);
        MessageCreator.createMsgCharQuest(p, 2);
    }

    // ------------------------------------------------------------------ nhac cua ai

    /**
     * Ha quai {@code mt}: neu la quai cua buoc cua ai TIEP THEO ma nguoi choi chua nhan (va nhan duoc ngay), hoi MOT lan /
     * buoc / phien bang hop 95 (Đồng ý = nhan, KhoaBanDoGame.khiTraLoi).
     */
    public static void khiHaQuai(final Char p, final Monster mt) {
        if (!LauDai.bat() || p == null || mt == null || p.charDBID <= 0 || p.isBot != -1) {
            return;
        }
        try {
            final ConcurrentHashMap<String, long[]> t = NhiemVuEngine.td(p);
            if (t == null) {
                return;
            }
            final String id = String.valueOf(mt.idTemplate);
            for (final NhiemVuMau m : NhiemVuEngine.MAU.values()) {
                if (!m.laCuaAi() || !"giet".equals(m.kieu) || t.containsKey(m.ma) || !m.khop("giet", id)) {
                    continue;
                }
                if (m.banDoChi() >= 0 && (p.map == null || p.map.mapIDLoadMap != m.banDoChi())) {
                    continue;
                }
                final java.util.Set<String> da = DA_NHAC.computeIfAbsent(Integer.valueOf(p.charDBID),
                        k -> java.util.Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>()));
                if (da.contains(m.ma)) {
                    continue;
                }
                // buoc cuoi cua chuong -> buoc tiep can lam; chi nhac khi dung buoc nay va nhan duoc ngay
                final String maCuoi = maCuoiChuong(m.ma);
                final NhiemVuEngine.BuocCuaAi b = NhiemVuEngine.buocCuaAi(p, maCuoi);
                if (b == null || b.m != m || b.v != null || b.loiNhan != null) {
                    continue;
                }
                da.add(m.ma);
                final String hop = MucTieuNhiemVu.nhacCuaAi(tenQuai(mt.idTemplate), tenCuaAi(maCuoi, m), b.so, b.tong,
                        KhoaBanDo.moTaBuoc(m.mucTieuText, m.ten));
                KhoaBanDoGame.hoiNhac(p, maCuoi, hop);
                return;
            }
        } catch (final Exception e) {
            System.out.println("MucTieuNhiemVuGame.khiHaQuai: " + e);
        }
    }

    static String maCuoiChuong(final String ma) {
        final int c = ma.lastIndexOf('.');
        final String tien = ma.substring(0, c + 1);
        String cuoi = ma;
        int so = NhiemVuEngine.soBuoc(ma);
        for (final NhiemVuMau x : NhiemVuEngine.MAU.values()) {
            if (x.laCuaAi() && x.ma.startsWith(tien) && NhiemVuEngine.soBuoc(x.ma) > so) {
                so = NhiemVuEngine.soBuoc(x.ma);
                cuoi = x.ma;
            }
        }
        return cuoi;
    }

    static String tenCuaAi(final String maCuoi, final NhiemVuMau m) {
        for (final KhoaBanDo.Luat l : KhoaBanDo.bang().values()) {
            if (maCuoi.equals(l.ma)) {
                return KhoaBanDo.tenCuaAi(l);
            }
        }
        return m.ten;
    }

    public static void khiThoat(final Char p) {
        if (p != null) {
            DA_NHAC.remove(Integer.valueOf(p.charDBID));
        }
    }

    /** Test. */
    public static void xoaNhac(final int idChar) {
        DA_NHAC.remove(Integer.valueOf(idChar));
    }
}
