package real;

import com.htbb.pokemon.VeDichChuyen;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * VE DICH CHUYEN - phan GAME (luat thuan: com.htbb.pokemon.VeDichChuyen). Chi dung goi / menu co san nen chay tren moi
 * client dang phat hanh (0.9.13..0.9.15, J2ME):
 *
 * <pre>
 *  MUA   Kecleon tap hoa (NPC tinh 1 / 26, goi 23) -> menu -30 "Mua thuốc, vật phẩm" (cua so thuoc cu) / "Vé dịch chuyển
 *        (N xu/vé)" -> menu {@link #NPC_MUA} (idMenu = cap luc xem gia) "Mua 1 / 5 / 10 vé: trả X xu" -> tru xu bang
 *        Char.truNeuDu + subXu (XU_DEM: dem la xu ra / sink, LauDai.ghiXuRa) + saveLogThongKeChiTieu nhu cua hang thuoc.
 *  DUNG  Hanh trang -> Sử dụng (goi 22, DungVatPham.dungThuoc) -> KHONG tru, mo menu {@link #NPC_VE} idMenu 0:
 *          "Về làng <lang vung minh>" / "Thành phố đã mở (n)" / "Huỷ"
 *        "Thành phố đã mở" -> idMenu 1 + trang: {@link VeDichChuyen#MOI_TRANG} diem / trang + Trang sau / Trang trước.
 *        Diem den: lang khoi dau (0 / 70 / 80), diem bay Pidgeot Kanto (RealController.xaphu), thi tran Hoenn co Trung tam
 *        (NpcDichVu.diemBay) - BO ban do dang dung va moi ban do KhoaBanDoGame.lyDoChan (cap / cua ai) chan.
 *        Chon -> kiem lai MOI dieu kien + khoa ban do, tru 1 ve, dich chuyen bang Map.move2Map (duong cua Pidgeot / Arcanine,
 *        cung diem xuat hien); khong toi noi (move2Map tu chan) thi tra lai ve. Bam lien / hai goi cung luc: khoa theo nhan
 *        vat ({@link #DANG}) + hoi chieu 60 giay -&gt; khong bao gio tru hai ve.
 * </pre>
 */
public final class VeDichChuyenGame {

    /** NPC ao menu dung ve (diem den). */
    public static final int NPC_VE = -590;
    /** NPC ao menu mua ve (idMenu = cap luc hien gia). */
    public static final int NPC_MUA = -591;
    /** Kecleon tap hoa (data_npc 1) va Kecleon ban le Cao nguyen (26). */
    public static final int NPC_KECLEON = 1, NPC_KECLEON_2 = 26;
    public static final String MUC_KECLEON_THUOC = "Mua thuốc, vật phẩm";

    /** charDBID -> luc dung thanh cong gan nhat (hoi chieu, giu qua dang nhap lai trong phien may chu). */
    static final ConcurrentHashMap<Integer, Long> LAN_CUOI = new ConcurrentHashMap<Integer, Long>();
    /** charDBID dang xu ly mot lan chon diem (chan hai goi chon cung luc). */
    static final java.util.Set<Integer> DANG = ConcurrentHashMap.newKeySet();
    /** charDBID -> danh sach diem {map, x, y} vua hien o menu "Thành phố đã mở" (chon theo chi so da hien). */
    static final ConcurrentHashMap<Integer, List<int[]>> DA_HIEN = new ConcurrentHashMap<Integer, List<int[]>>();

    /** Cach dich chuyen (test thay bang ham gia; game: Map.move2Map). */
    public interface DiChuyen {
        void di(Char p, int x, int y, int mapId);
    }

    public static volatile DiChuyen DI = new DiChuyen() {
        @Override
        public void di(final Char p, final int x, final int y, final int mapId) {
            p.map.move2Map(p, x, y, mapId, p.inCountry);
        }
    };

    /** Ban do co ton tai (test thay; game: RealController.mapList). */
    public interface CoBanDo {
        boolean co(int mapId);
    }

    public static volatile CoBanDo CO = new CoBanDo() {
        @Override
        public boolean co(final int mapId) {
            return RealController.mapList != null && RealController.mapList.get(Integer.valueOf(mapId)) != null;
        }
    };

    private VeDichChuyenGame() {
    }

    // ================================================================== Kecleon / mua

    /** Menu Kecleon (goi 23 loai 1 / 26): cua so thuoc cu, ve dich chuyen, thuoc cap cao (real.ThuocCapCaoGame). */
    public static String[] menuKecleon(final Char p) {
        return new String[]{MUC_KECLEON_THUOC, VeDichChuyen.TEN + " (" + so(VeDichChuyen.gia(cap(p))) + " xu/vé)",
            ThuocCapCaoGame.TEN_MUC};
    }

    public static void moMua(final Char p) {
        final int cap = cap(p);
        final long gia = VeDichChuyen.gia(cap);
        final String[] m = new String[VeDichChuyen.SO_MUA.length + 1];
        for (int i = 0; i < VeDichChuyen.SO_MUA.length; i++) {
            m[i] = "Mua " + VeDichChuyen.SO_MUA[i] + " vé: trả " + so(gia * VeDichChuyen.SO_MUA[i]) + " xu";
        }
        m[VeDichChuyen.SO_MUA.length] = "Huỷ";
        DungVatPham.baoNhe(p, VeDichChuyen.TEN + ": " + so(gia) + " xu/vé (theo cấp " + cap + "), dùng 1 lần. Đang có "
                + co(p) + "/" + VeDichChuyen.GIU_TOI_DA + ".");
        p.sendMessage(MessageCreator.createMsgMenu(m, NPC_MUA, Math.max(0, Math.min(255, cap))));
    }

    /** Chon goi mua. true = da tru xu va cong ve. */
    public static boolean mua(final Char p, final int capHien, final int chon) {
        if (p == null || chon < 0 || chon >= VeDichChuyen.SO_MUA.length) {
            return false;
        }
        if (cap(p) != Math.min(255, capHien)) {
            DungVatPham.loi(p, "Giá đã đổi theo cấp mới, xem lại giá.");
            moMua(p);
            return false;
        }
        final int n = VeDichChuyen.SO_MUA[chon];
        final long tong = VeDichChuyen.gia(cap(p)) * n;
        final int[] daCo = new int[1];
        final boolean[] day = new boolean[1];
        final boolean ok = p.truNeuDu(() -> {
            daCo[0] = p.potions[VeDichChuyen.ID];
            day[0] = VeDichChuyen.vuotTui(daCo[0], n);
            return !day[0] && p.getxu() >= tong;
        }, () -> {
            p.subXu(tong, true, "mua ve dich chuyen"); // XU_DEM: xu ra (sink)
            p.potions[VeDichChuyen.ID] += n;
        });
        if (!ok) {
            DungVatPham.loi(p, day[0] ? "Túi chỉ giữ tối đa " + VeDichChuyen.GIU_TOI_DA + " " + VeDichChuyen.TEN + " (đang có " + daCo[0] + ")."
                    : "Không đủ " + so(tong) + " xu để mua " + n + " " + VeDichChuyen.TEN + ".");
            return false;
        }
        try {
            data.Database.instance.saveLogThongKeChiTieu(VeDichChuyen.TEN, 0, n, tong, "xu");
        } catch (final Throwable ignored) {
        }
        ghi("mua " + p.charname + " cap " + cap(p) + " x" + n + " = " + tong + " xu");
        try {
            p.sendMessage(MessageCreator.createMainCharInfoMessage(p));
            p.sendMessage(MessageCreator.createCharInventoryMessage(p, 0));
        } catch (final Throwable ignored) {
        }
        DungVatPham.bao(p, "Đã mua " + n + " " + VeDichChuyen.TEN + ", trả " + so(tong) + " xu. Dùng trong Hành trang.");
        return true;
    }

    // ================================================================== dung

    /** Ly do khong dung duoc luc nay (null = duoc). Khong gui gi. */
    public static String lyDoChan(final Char p) {
        if (p == null) {
            return "";
        }
        final boolean guc = p.hp <= 0 || p.isDie();
        final boolean gym = dangGym(p);
        final boolean rieng = phongRieng(p);
        final boolean pvp = pvp(p);
        final boolean hang = giaoHang(p);
        final boolean pk = p.pk != 0 || p.isKiller || p.killer > 0;
        final Long l = LAN_CUOI.get(Integer.valueOf(p.charDBID));
        final String ly = VeDichChuyen.lyDo(guc, gym, rieng, pvp, hang, pk,
                VeDichChuyen.giayCho(l == null ? 0L : l.longValue(), System.currentTimeMillis()));
        if (ly != null) {
            return ly;
        }
        if (p.userTrade != null && p.userTrade.size() > 0) {
            return "Đang giao dịch: xong giao dịch rồi hãy dùng " + VeDichChuyen.TEN + ".";
        }
        try {
            if (p.freeze()) {
                return "Đang bị khống chế: chờ hết hiệu ứng rồi hãy dùng " + VeDichChuyen.TEN + ".";
            }
        } catch (final Throwable ignored) {
        }
        return null;
    }

    static boolean dangGym(final Char p) {
        try {
            return BossLauDai.gymDangDanh(p) > 0;
        } catch (final Throwable e) {
            return false;
        }
    }

    /**
     * Pho ban / san cup (phong rieng 0.9.7), ham nguc / pho ban cu (Dun, DungeonMap, PrivateMap), Nui chau bau, Van tien
     * tran, ban do boss, khu Le ket doi (225 / 226). Khu mo, lanh dia doi, Cao nguyen ngoai gio chien: dung duoc.
     */
    static boolean phongRieng(final Char p) {
        if (com.htbb.pokemon.PhongRieng.laMapRieng(p.mapID)) {
            return true;
        }
        final Map m = p.map;
        if (m == null) {
            return true;
        }
        if (m instanceof MapPhongRieng || m instanceof Dun || m instanceof DungeonMap || m instanceof PrivateMap
                || m instanceof MapChauBau || m instanceof MapVanTienTran || m instanceof MapBoss
                || com.htbb.pokemon.PhongRieng.laMapRieng(m.mapId)) {
            return true;
        }
        try {
            return m.isMapNuiChauBau() || m.isvanTienTran() || m.isMapBoss() || p.mapID == 225 || p.mapID == 226;
        } catch (final Throwable e) {
            return false;
        }
    }

    static boolean pvp(final Char p) {
        try {
            if (BanHuongGame.laMapPvp(p.map, p)) {
                return true;
            }
        } catch (final Throwable ignored) {
        }
        try {
            // Dau truong La Ma 203, Dau truong 2 / 3, San dau / Sanh cho, Tong kim, chien truong
            if (p.map != null && (p.map.isMapChienTruongMoba() || p.map instanceof MapTongKim || p.map.mapIDLoadMap == 203
                    || p.map.isMapLoiDai() || p.map.isMapChoLoiDai())) {
                return true;
            }
        } catch (final Throwable ignored) {
        }
        try {
            if (XepHangPvpGame.trongTran(p) || CupGame.dangThiDau(p.charDBID) || Pk3v3Game.dangThiDau(p.charDBID)
                    || DauTruongGame.TRAN.containsKey(Integer.valueOf(p.charDBID))) {
                return true;
            }
        } catch (final Throwable ignored) {
        }
        try {
            return BanHuongGame.trongPvp(p);
        } catch (final Throwable e) {
            return false;
        }
    }

    static boolean giaoHang(final Char p) {
        if (p.monster != null) {
            return true;
        }
        try {
            return p.char_quest != null && p.char_quest.receive == 1 && QuestTemplate.getTypeQuest(p.char_quest.id_quest) == 1;
        } catch (final Throwable e) {
            return false;
        }
    }

    /** DungVatPham.dungThuoc (goi 22, id {@link VeDichChuyen#ID}): KHONG tru - mo menu diem den hoac bao ly do. */
    public static void dungTuTui(final Char p) {
        guiTui(p); // client cu tru 1 o truoc khi gui goi 22: tra lai so that
        if (p.potions[VeDichChuyen.ID] <= 0) {
            DungVatPham.loi(p, "Không còn " + VeDichChuyen.TEN + ".");
            return;
        }
        final String ly = lyDoChan(p);
        if (ly != null) {
            DungVatPham.loi(p, ly);
            return;
        }
        moGoc(p);
    }

    /** Menu goc (3 dong, vua mot man J2ME). */
    public static void moGoc(final Char p) {
        final int[] lang = lang(p);
        final List<int[]> ds = diemDaMo(p);
        p.sendMessage(MessageCreator.createMsgMenu(new String[]{
            "Về làng " + ten(lang[0]) + (lang[0] == banDoDang(p) ? " (đang ở đây)" : ""),
            "Thành phố đã mở (" + ds.size() + ")",
            "Huỷ",
        }, NPC_VE, 0));
    }

    static void moTrang(final Char p, final int trang) {
        final List<int[]> ds = diemDaMo(p);
        if (ds.isEmpty()) {
            DungVatPham.loi(p, "Chưa có thành phố nào khác đã mở. Lên cấp và qua cửa ải để mở thêm bản đồ.");
            moGoc(p);
            return;
        }
        DA_HIEN.put(Integer.valueOf(p.charDBID), ds);
        final String[] ten = new String[ds.size()];
        for (int i = 0; i < ten.length; i++) {
            ten[i] = nhan(ds.get(i)[0]);
        }
        final int tr = VeDichChuyen.kepTrang(ten.length, trang);
        p.sendMessage(MessageCreator.createMsgMenu(VeDichChuyen.menuTrang(ten, tr), NPC_VE, 1 + tr));
    }

    /** MapDoMenu (qua NpcDichVu.xuLyMenu): true = menu cua lop nay. */
    public static boolean xuLyMenu(final Char p, final int idNpc, final int idMenu, final int chon) {
        if (idNpc == NPC_MUA) {
            mua(p, idMenu, chon);
            return true;
        }
        if (idNpc != NPC_VE) {
            return false;
        }
        if (p == null) {
            return true;
        }
        if (idMenu == 0) {
            if (chon == 0) {
                final int[] l = lang(p);
                chonDiem(p, l);
            } else if (chon == 1) {
                moTrang(p, 0);
            }
            return true;
        }
        final int tr = idMenu - 1;
        final List<int[]> ds = DA_HIEN.get(Integer.valueOf(p.charDBID));
        if (ds == null || ds.isEmpty()) {
            moTrang(p, 0);
            return true;
        }
        final int i = VeDichChuyen.chiSo(ds.size(), tr, chon);
        if (i >= 0) {
            chonDiem(p, ds.get(i));
        } else if (i == -1) {
            moTrang(p, tr + 1);
        } else if (i == -2) {
            if (tr > 0) {
                moTrang(p, tr - 1);
            } else {
                moGoc(p);
            }
        }
        return true;
    }

    /**
     * Dich chuyen toi {map, x o, y o}: kiem lai moi dieu kien (menu co the cu), tru 1 ve, move2Map; khong toi noi thi tra
     * lai ve. true = da toi va da tru dung 1 ve.
     */
    public static boolean chonDiem(final Char p, final int[] d) {
        final Integer k = Integer.valueOf(p.charDBID);
        if (!DANG.add(k)) {
            return false; // goi chon thu hai trong luc goi dau dang chay
        }
        try {
            final String ly = lyDoChan(p);
            if (ly != null) {
                DungVatPham.loi(p, ly);
                return false;
            }
            final int den = d[0];
            if (den == banDoDang(p)) {
                DungVatPham.loi(p, "Bạn đang ở " + ten(den) + " rồi.");
                return false;
            }
            if (!CO.co(den)) {
                DungVatPham.loi(p, ten(den) + " đang tạm đóng.");
                return false;
            }
            if (KhoaBanDoGame.chan(p, den)) {
                return false; // cap / cua ai: KhoaBanDoGame da bao ly do, khong tru ve
            }
            final String hoenn = lyDoHoenn(p, den);
            if (hoenn != null) {
                DungVatPham.loi(p, hoenn); // cong tau Hoenn (VungHoenn.chanVao): ve khong nhay qua
                return false;
            }
            synchronized (p) {
                if (p.potions[VeDichChuyen.ID] <= 0) {
                    DungVatPham.loi(p, "Không còn " + VeDichChuyen.TEN + ".");
                    return false;
                }
                p.potions[VeDichChuyen.ID]--;
            }
            final int tu = banDoDang(p);
            final long bay = System.currentTimeMillis();
            LAN_CUOI.put(k, Long.valueOf(bay));
            boolean toi = false;
            try {
                DI.di(p, d[1], d[2], den);
                toi = banDoDang(p) == den || (p.map != null && p.map.mapIDLoadMap == den) || p.mapID == den;
            } catch (final Throwable e) {
                com.htbb.log.Nhat.loi("VE_DICH_CHUYEN", "dich chuyen " + p.charname + " -> " + den, e);
            }
            if (!toi) {
                synchronized (p) {
                    p.potions[VeDichChuyen.ID]++;
                }
                LAN_CUOI.remove(k, Long.valueOf(bay));
                guiTui(p);
                DungVatPham.loi(p, "Chưa dịch chuyển được tới " + ten(den) + ". Vé vẫn còn nguyên.");
                return false;
            }
            DA_HIEN.remove(k);
            ghi("dung " + p.charname + " cap " + cap(p) + " " + tu + " -> " + den + " con " + p.potions[VeDichChuyen.ID]);
            guiTui(p);
            DungVatPham.bao(p, "Đã dịch chuyển tới " + ten(den) + ". Còn " + p.potions[VeDichChuyen.ID] + " " + VeDichChuyen.TEN + ".");
            return true;
        } finally {
            DANG.remove(k);
        }
    }

    // ================================================================== diem den

    /** Lang khoi dau cua vung nhan vat {map, x, y} (cung bang lang voi Arcanine "Về làng" / hoi sinh). */
    public static int[] lang(final Char p) {
        final int[][] b = LangKhoiDau.bang(p);
        final int den = b[Math.max(0, Math.min(b.length - 1, p.myCountry))][0];
        final int[] vt = LangKhoiDau.viTri(den);
        return new int[]{den, vt != null ? vt[0] : 16, vt != null ? vt[1] : 24};
    }

    /** Moi diem co the toi (chua loc khoa / ban do dang dung): lang, bay Pidgeot Kanto, thi tran Hoenn co Trung tam. */
    static List<int[]> moiDiem() {
        final List<int[]> kq = new ArrayList<int[]>();
        final java.util.Set<Integer> da = new java.util.HashSet<Integer>();
        for (final int l : new int[]{com.htbb.pokemon.LangKhoiDauLuat.PEWTER, com.htbb.pokemon.LangKhoiDauLuat.BLACKTHORN,
            com.htbb.pokemon.LangKhoiDauLuat.CIANWOOD}) {
            final int[] vt = LangKhoiDau.viTri(l);
            if (vt != null && da.add(Integer.valueOf(l))) {
                kq.add(new int[]{l, vt[0], vt[1]});
            }
        }
        final java.util.TreeMap<Integer, int[]> kanto = new java.util.TreeMap<Integer, int[]>();
        if (RealController.xaphu != null) {
            for (final XaphuTemplate t : RealController.xaphu) {
                for (int j = 0; t != null && j < t.mapID.length && j < t.x.length && j < t.y.length; j++) {
                    final int id = t.mapID[j];
                    if (!da.contains(Integer.valueOf(id)) && !kanto.containsKey(Integer.valueOf(id))) {
                        kanto.put(Integer.valueOf(id), new int[]{id, t.x[j], t.y[j]});
                    }
                }
            }
        }
        for (final int[] d : kanto.values()) {
            da.add(Integer.valueOf(d[0]));
            kq.add(d);
        }
        try {
            for (final com.htbb.pokemon.BanDoHoenn.BanDo b : com.htbb.pokemon.BanDoHoenn.ds()) {
                if (!com.htbb.pokemon.HoiSinh.coTrungTam(b) || !da.add(Integer.valueOf(b.id))) {
                    continue;
                }
                final int[] o = com.htbb.pokemon.HoiSinh.diemTrungTamHoenn(b.id, new com.htbb.pokemon.HoiSinh.DiDuoc() {
                    @Override
                    public boolean o(final int map, final int x, final int y) {
                        final Map m = RealController.mapList == null ? null : RealController.mapList.get(Integer.valueOf(map));
                        return m == null || m.canMove(x * 16 + 8, y * 16 + 8);
                    }
                });
                if (o != null && o[0] == b.id) {
                    kq.add(new int[]{b.id, o[1], o[2]});
                }
            }
        } catch (final Throwable e) {
            // Hoenn chua nap: chi Kanto / Johto
        }
        return kq;
    }

    /** Diem "Thành phố đã mở": bo lang vung minh (dong rieng o menu goc), ban do dang dung, ban do khong co / bi khoa. */
    public static List<int[]> diemDaMo(final Char p) {
        final int nha = lang(p)[0];
        final int dang = banDoDang(p);
        final List<int[]> kq = new ArrayList<int[]>();
        for (final int[] d : moiDiem()) {
            if (d[0] == nha || d[0] == dang || !CO.co(d[0]) || KhoaBanDoGame.lyDoChan(p, d[0]) != null || lyDoHoenn(p, d[0]) != null) {
                continue;
            }
            kq.add(d);
        }
        return kq;
    }

    /**
     * Cong vao Hoenn (giong VungHoenn.chanVao, khong gui gi): tu ngoai Hoenn toi ban do Hoenn can cap
     * BanDoHoenn.CAP_VAO, tran cap mua va xong cot truyen nam 1 (lau dai). null = vao duoc.
     */
    static String lyDoHoenn(final Char p, final int den) {
        try {
            if (p.isAdmin || !com.htbb.pokemon.BanDoHoenn.laHoenn(den) || com.htbb.pokemon.BanDoHoenn.laHoenn(banDoDang(p))) {
                return null;
            }
            final boolean lauDai = com.htbb.pokemon.LauDai.bat();
            final int tran = lauDai ? com.htbb.pokemon.CapDoLauDai.tranCap(com.htbb.pokemon.LauDai.ngayHienTai(), LevelDetail.expMain.length) : 0;
            return com.htbb.pokemon.BanDoHoenn.lyDoChan(lauDai, tran, cap(p),
                    !lauDai || NhiemVuEngine.daXongMa(p, com.htbb.pokemon.BanDoHoenn.MA_KET_NAM_1));
        } catch (final Throwable e) {
            return com.htbb.pokemon.BanDoHoenn.laHoenn(den) && cap(p) < com.htbb.pokemon.BanDoHoenn.CAP_VAO
                    ? "Vùng Hoenn cần đạt cấp " + com.htbb.pokemon.BanDoHoenn.CAP_VAO + " trở lên." : null;
        }
    }

    // ================================================================== tien ich

    static int banDoDang(final Char p) {
        if (p.map == null) {
            return p.mapID;
        }
        return p.map.isMapOffline ? p.mapID : p.map.mapIDLoadMap;
    }

    static int cap(final Char p) {
        try {
            return p.lvDetail.lv;
        } catch (final Throwable e) {
            return 1;
        }
    }

    static int co(final Char p) {
        return p.potions == null || p.potions.length <= VeDichChuyen.ID ? 0 : p.potions[VeDichChuyen.ID];
    }

    static String ten(final int mapId) {
        try {
            final String t = NpcDichVu.tenBanDo(mapId)[0];
            return t == null || t.isEmpty() ? "bản đồ " + mapId : t;
        } catch (final Throwable e) {
            return "bản đồ " + mapId;
        }
    }

    /** Nhan dong menu: ten + vung (Kanto / Johto / Hoenn). */
    static String nhan(final int mapId) {
        final String t = ten(mapId);
        final String v;
        if (com.htbb.pokemon.BanDoHoenn.laHoenn(mapId)) {
            v = "Hoenn";
        } else if (mapId == com.htbb.pokemon.LangKhoiDauLuat.BLACKTHORN || mapId == com.htbb.pokemon.LangKhoiDauLuat.CIANWOOD) {
            v = "Johto";
        } else {
            v = "Kanto";
        }
        return t.contains(v) ? t : t + " (" + v + ")";
    }

    static String so(final long n) {
        return com.htbb.pokemon.HoiSinh.so(n);
    }

    static void guiTui(final Char p) {
        try {
            p.sendMessage(MessageCreator.createCharInventoryMessage(p, 0));
        } catch (final Throwable ignored) {
        }
    }

    static void ghi(final String s) {
        try {
            com.htbb.log.Nhat.thongTin("VE_DICH_CHUYEN", s);
        } catch (final Throwable ignored) {
        }
    }

    /** Test: xoa trang thai theo nhan vat. */
    public static void quen(final int charDBID) {
        LAN_CUOI.remove(Integer.valueOf(charDBID));
        DANG.remove(Integer.valueOf(charDBID));
        DA_HIEN.remove(Integer.valueOf(charDBID));
    }
}
