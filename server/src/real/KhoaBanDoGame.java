package real;

import com.htbb.cauhinh.CauHinhTrungTam;
import com.htbb.pokemon.KhoaBanDo;
import io.Message;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Noi KHOA BAN DO (com.htbb.pokemon.KhoaBanDo, bang data_ban_do_mo_khoa) vao game: chan moi duong vao ban do
 * (doChangeMap, move2Map, xa phu, Pidgeot / Lapras, bai luyen...), mien tru nhan vat cu luc nap (tob_ban_do_mo), gui
 * danh sach ban do khoa / khong cho tu dong cho client (goi {@link #LENH} -108) va bao "Đã mở bản đồ" khi len cap /
 * xong cua ai. Admin khong bi chan. Tat ca bang cong tac {@link #K_BAT} (danh muc cau hinh, mac dinh 1).
 */
public final class KhoaBanDoGame {

    public static final String K_BAT = "ban_do.khoa.bat";
    public static final int LENH = -108;
    /** Client dau tien doc goi -108: BAN 908 (0.9.8). Ban 906 / 907 da phat hanh KHONG co bo doc - chi nhan the -104. */
    public static final int BAN_CLIENT = 908;

    /** charDBID -> moc mien tru (giu ca khi thoat: vai so nguyen / nhan vat, tranh dua giua phien cu va moi). */
    private static final ConcurrentHashMap<Integer, Integer> MIEN = new ConcurrentHashMap<Integer, Integer>();
    /** charDBID -> ban do goc dang khoa lan gui truoc (bao "Đã mở"). */
    private static final ConcurrentHashMap<Integer, Set<Integer>> KHOA_TRUOC = new ConcurrentHashMap<Integer, Set<Integer>>();
    /** charDBID -> van tay goi -108 lan truoc (khong gui lai neu khong doi). */
    private static final ConcurrentHashMap<Integer, Integer> VAN_TAY = new ConcurrentHashMap<Integer, Integer>();

    private KhoaBanDoGame() {
    }

    /** Khoi dong server (RealController sau TenBanDoMayChu): tao bang mien tru, nap luat, nap lai tu dong. */
    public static void batDau() {
        KhoaBanDo.taoBang();
        KhoaBanDo.nap();
        KhoaBanDo.batNapLaiTuDong(new Runnable() {
            @Override
            public void run() {
                capNhatTatCa();
            }
        });
    }

    public static boolean bat() {
        return !"0".equals(CauHinhTrungTam.chuoi(K_BAT).trim());
    }

    /** Ban do goc (mapIDLoadMap) cua id bat ky (ban sao theo khu theo luat ban goc). */
    static int goc(final int mapId) {
        final Map m = RealController.mapList == null ? null : RealController.mapList.get(Integer.valueOf(mapId));
        return m != null ? m.mapIDLoadMap : mapId;
    }

    public static KhoaBanDo.Luat luatCua(final int mapId) {
        return KhoaBanDo.luat(goc(mapId));
    }

    static int capCua(final Char p) {
        try {
            return p.lvDetail.lv;
        } catch (final Exception e) {
            return 0;
        }
    }

    static int mien(final Char p) {
        final Integer m = MIEN.get(Integer.valueOf(p.charDBID));
        // chua nap (vd test / nhan vat ao): mien tru theo cap hien tai - an toan, khong bao gio khoa nham
        return m != null ? m.intValue() : Math.max(1, capCua(p));
    }

    static boolean daXong(final Char p, final KhoaBanDo.Luat l) {
        return l.ma.isEmpty() || NhiemVuEngine.daXongMa(p, l.ma);
    }

    static String tenMap(final int mapId) {
        try {
            final String t = TenBanDoMayChu.tenDayDu(mapId);
            return t.isEmpty() ? TenBanDoMayChu.ten(goc(mapId)) : t;
        } catch (final Exception e) {
            return "";
        }
    }

    /**
     * null = vao duoc; nguoc lai cau bao. Khong gui gi. Tram trung chuyen (thuyen 107 / 2441..2444): xet ban do thuyen
     * cap ben ({@link #denCuaThuyen}) - khong cho len thuyen ma ben kia con khoa (sua-ket-tau).
     */
    public static String lyDoChan(final Char p, final int toMapId) {
        if (p == null || p.isAdmin || !bat() || toMapId == p.mapID) {
            return null;
        }
        final int dich = dichThuc(toMapId);
        if (dich == p.mapID) {
            return null;
        }
        return lyDoKhoa(p, dich);
    }

    /** Nhu {@link #lyDoChan} nhung KHONG mien ban do dang dung (kiem nhan vat dang dung o dau luc nap). */
    static String lyDoKhoa(final Char p, final int mapId) {
        if (p == null || p.isAdmin || !bat()) {
            return null;
        }
        final KhoaBanDo.Luat l = luatCua(mapId);
        if (l == null || !l.coKhoa()) {
            return null;
        }
        return KhoaBanDo.lyDo(l, capCua(p), daXong(p, l), mien(p), tenMap(mapId));
    }

    // ------------------------------------------------------------------ tram trung chuyen (thuyen Ben tau -> New Bark)

    /** Thuyen (TeamServer: Ben tau 106 / 2421..2424 -> thuyen 107 / 2441..2444 -> New Bark 10 / 501..504). */
    public static boolean laTramTrungChuyen(final int mapId) {
        return mapId == 107 || mapId >= 2441 && mapId <= 2444 || mapId > 0 && goc(mapId) == 107;
    }

    /** Ban do thuyen cap ben (MapVongLap / Dun: 107 -> 10, 244k -> 50k). Khong phai thuyen: chinh no. */
    public static int denCuaThuyen(final int mapThuyen) {
        if (mapThuyen >= 2441 && mapThuyen <= 2444) {
            return mapThuyen % 2440 + 500;
        }
        return laTramTrungChuyen(mapThuyen) ? 10 : mapThuyen;
    }

    /** Ben len thuyen (CharVongLap: 106 -> 107, 242k -> 244k). */
    public static int benCuaThuyen(final int mapThuyen) {
        return mapThuyen >= 2441 && mapThuyen <= 2444 ? mapThuyen - 20 : 106;
    }

    /** Thuyen cua ben (Map.doUp2Board / CharVongLap). */
    public static int thuyenCuaBen(final int ben) {
        return ben >= 2421 && ben <= 2424 ? ben + 20 : 107;
    }

    /** Ban do that se toi: thuyen -> ben kia, con lai chinh no. */
    static int dichThuc(final int mapId) {
        return laTramTrungChuyen(mapId) ? denCuaThuyen(mapId) : mapId;
    }

    /**
     * Thuyen cap ben (MapVongLap.update / updateWind / updateSand, Dun.update - truoc day move2Map(den) truc tiep): ben kia
     * mo -> xuong thuyen (tru 1 ve nhu cu); ben kia KHOA -> dua ve ben xuat phat MOT lan (giu ve), bao ly do + hop cua ai sau
     * khi nap map. Truoc day move2Map bi chan -> nap lai thuyen tai cho, vong lap ban do (300 ms) goi lai ngay -> nap map
     * lien tuc, moi hop / menu bi dong (GameScr.onMap), ket mai tren thuyen (bao loi kazuhas4 24/10/2026).
     */
    public static void xuongThuyen(final Map map, final Char p, final int den) {
        final String ly = lyDoChan(p, den);
        if (ly == null) {
            map.move2Map(p, 62 + Map.random(5), 122 + Map.random(5), den, p.inCountry);
            if (p.potions[20] > 0) {
                p.potions[20]--;
            }
            return;
        }
        veNoiAnToan(map, p, den, ly);
    }

    /** Dua nhan vat khoi thuyen ve ben xuat phat (ben khoa / khong co -> lang khoi dau cua vung). */
    static void veNoiAnToan(final Map map, final Char p, final int den, final String ly) {
        int ben = benCuaThuyen(p.mapID);
        int x = 16;
        int y = 24;
        final Map mBen = RealController.mapList == null ? null : RealController.mapList.get(Integer.valueOf(ben));
        int[] o = null;
        try {
            o = NpcTinhBanDo.oNpc(goc(ben), NpcDichVu.NPC_LAPRAS);
        } catch (final Throwable ignored) {
        }
        if (mBen == null || lyDoKhoa(p, ben) != null) {
            final int[] l = lang(p);
            ben = l[0];
            x = l[1];
            y = l[2];
        } else if (o != null) {
            x = o[0];
            y = o[1] + 2;
        }
        try {
            baoChan(p, den, ly, true); // hop cua ai cho toi khi nap xong ben (-500), khong mo roi bi dong
        } catch (final Throwable ignored) {
        }
        map.move2Map(p, x, y, ben, p.inCountry);
        final String tenBen = tenMap(ben);
        final String tenDen = tenMap(den);
        bao(p, "Chưa mở " + (tenDen.isEmpty() ? "bến bên kia" : tenDen) + ": tàu đã đưa bạn về "
                + (tenBen.isEmpty() ? "bến" : tenBen) + ". Hoàn thành cửa ải rồi lên tàu lại.");
        ghi("thuyen " + p.charname + " cap " + capCua(p) + " ben " + den + " khoa -> ve " + ben);
    }

    /** {map, x o, y o} lang khoi dau cua vung (nhu CharNapDuLieu / NpcDichVu "Về làng"). */
    static int[] lang(final Char p) {
        try {
            final int[][] ds = LangKhoiDau.bang(p);
            final int vung = Math.max(0, Math.min(ds.length - 1, p.myCountry));
            final int den = ds[vung][0];
            final int[] vt = LangKhoiDau.viTri(den);
            return new int[]{den, vt != null ? vt[0] : 16, vt != null ? vt[1] : 24};
        } catch (final Throwable e) {
            return new int[]{0, 16, 24};
        }
    }

    static void ghi(final String s) {
        System.out.println("[KHOA BAN DO] " + s);
        try {
            com.htbb.log.Nhat.thongTin("KHOA_MAP", s);
        } catch (final Throwable ignored) {
        }
    }

    // ------------------------------------------------------------------ move2Map: chong nap lai lien tuc

    /** Trong chung nay sau mot lan move2Map bi chan + nap lai tai cho, move2Map bi chan tiep KHONG nap lai / bao nua. */
    static final long NAP_LAI_MS = 3000L;
    /** charDBID -> luc nap lai tai cho gan nhat (move2Map bi chan). */
    private static final ConcurrentHashMap<Integer, Long> NAP_LAI = new ConcurrentHashMap<Integer, Long>();

    /**
     * Map.move2Map (chot cuoi): 0 = di duoc; 1 = bi chan, nap lai tai cho (lan dau, bao + hop cua ai sau -500);
     * 2 = bi chan va vua nap lai trong {@link #NAP_LAI_MS}: nguoi goi bo qua (khong nap lai, khong bao) - mot duong may
     * chu goi lap (vong lap ban do, hen gio) khong lam client nap map lien tuc / dong menu.
     */
    public static int chanMove2Map(final Char p, final int mapId) {
        if (p == null || mapId == p.mapID || lyDoChan(p, mapId) == null) {
            return 0;
        }
        final Integer k = Integer.valueOf(p.charDBID);
        final long bay = bayGio();
        final Long cu = NAP_LAI.get(k);
        if (cu != null && bay - cu.longValue() < NAP_LAI_MS) {
            return 2;
        }
        NAP_LAI.put(k, Long.valueOf(bay));
        chanDiBo(p, mapId);
        return 1;
    }

    // ------------------------------------------------------------------ cuu nhan vat ket luc nap

    /** charDBID -> {cau bao, ban do chua mo} cho gui sau khi client nap map dau tien (-500). */
    private static final ConcurrentHashMap<Integer, Object[]> CUU = new ConcurrentHashMap<Integer, Object[]>();

    /**
     * Luc nap (sau {@link #khiNap} co moc mien): dang luu o thuyen ma ben kia khoa, hoac o ban do khoa voi nhan vat ->
     * dua ve lang khoi dau cua vung (doi p.mapID / x / y truoc khi vao map), ghi nhat ky, bao sau -500. true = da dua.
     */
    static boolean cuuKhiNap(final Char p) {
        if (p == null || p.isAdmin || !bat() || p.myCountry < 0) {
            return false;
        }
        final int dich = dichThuc(p.mapID);
        final KhoaBanDo.Luat l = luatCua(dich);
        if (l == null || !l.coKhoa()) {
            return false;
        }
        if (!l.ma.isEmpty() && com.htbb.pokemon.LauDai.bat() && NhiemVuEngine.td(p) == null) {
            return false; // CSDL nhiem vu loi: khong biet da xong cua ai chua -> khong dua ai di
        }
        final String ly = lyDoKhoa(p, dich);
        if (ly == null) {
            return false;
        }
        final int tu = p.mapID;
        final int[] lg = lang(p);
        p.mapID = lg[0];
        p.x = lg[1] * 16 + 8;
        p.y = lg[2] * 16 + 8;
        p.region = 0;
        p.inCountry = p.myCountry;
        final String tenLang = tenMap(lg[0]);
        final String tenDen = tenMap(dich);
        final String cau = "Bạn đã được đưa về " + (tenLang.isEmpty() ? "làng" : tenLang) + " vì chưa mở "
                + (tenDen.isEmpty() ? "bản đồ này" : tenDen) + ". " + ly;
        CUU.put(Integer.valueOf(p.charDBID), new Object[]{cau, Integer.valueOf(dich),
                "Bạn đã được đưa về " + (tenLang.isEmpty() ? "làng" : tenLang) + "."});
        ghi("cuu luc nap " + p.charname + " cap " + capCua(p) + " o map " + tu + " (" + dich + " khoa) -> " + lg[0]);
        return true;
    }

    /** sauKhiVaoMap: bao nhan vat vua duoc dua ve (hop cua ai kem cau bao neu co, khong thi hop thong bao). */
    static boolean baoCuu(final Char p) {
        final Object[] c = CUU.remove(Integer.valueOf(p.charDBID));
        if (c == null) {
            return false;
        }
        final String cau = (String) c[0];
        final long luc = bayGio();
        Hoi h = null;
        try {
            h = taoHoi(p, ((Integer) c[1]).intValue(), luc);
        } catch (final Throwable ignored) {
        }
        bao(p, cau);
        try {
            if (h != null && h.hop != null) {
                hoi(p, new Hoi(h.goc, h.maCuoi, h.chinh, h.phu, c[2] + "\n" + h.hop, luc), luc);
            } else {
                p.sendMessage(MessageCreator.createServerAlertMessage(cau, ""));
            }
        } catch (final Throwable ignored) {
        }
        return true;
    }

    /**
     * Chan + bao NGAY (duong khong nap lai map: menu NPC bay / do / Arcanine, bai luyen, vat pham). true = bi chan (nguoi
     * goi giu nguoi choi o cho cu, KHONG tru phi). Thieu cua ai -> hop xac nhan {@link #POPUP_CUA_AI} (xem {@link #baoChan}).
     */
    public static boolean chan(final Char p, final int toMapId) {
        return chan(p, toMapId, false);
    }

    /**
     * Nhu {@link #chan} cho duong ma client NAP LAI map hien tai sau khi bi chan (di bo qua cong goi 12, move2Map, xa phu):
     * GameScr.onMap dong moi hop dang mo, nen hop cua ai cho toi khi client bao nap xong (goi 12 toMapID -500,
     * {@link #sauKhiVaoMap}).
     */
    public static boolean chanDiBo(final Char p, final int toMapId) {
        return chan(p, toMapId, true);
    }

    static boolean chan(final Char p, final int toMapId, final boolean sauNapMap) {
        final String ly = lyDoChan(p, toMapId);
        if (ly == null) {
            return false;
        }
        baoChan(p, dichThuc(toMapId), ly, sauNapMap); // thuyen: hop cua ai cua ben kia
        try {
            com.htbb.log.Nhat.thongTin("KHOA_MAP", "chan " + p.charname + " cap " + capCua(p) + " -> map " + toMapId);
        } catch (final Throwable ignored) {
        }
        return true;
    }

    /**
     * Bi chan o duong khong tu nap lai map (xa phu / bay da chuyen client sang man cho): nap lai map hien tai tai cho
     * dung (createMapMessage) de client khong ket man cho 30 giay.
     */
    public static void giuTaiCho(final Char p) {
        try {
            if (p.map != null) {
                p.map.move2Map(p, p.x / 16, p.y / 16, p.mapID, p.inCountry);
            }
        } catch (final Exception e) {
            System.out.println("KhoaBanDoGame.giuTaiCho: " + e);
        }
    }

    // ------------------------------------------------------------------ hop cua ai (bi chan vi chua xong cua ai)

    /**
     * Popup -32 (MessageCreator.createMsgPopUp, client 0.9.7+ deu co: hop Có / Không, 0.9.3+ "Huỷ" / "Đồng ý"); tra loi o
     * Map.doPopup -> {@link #khiTraLoi}. 96 = bo qua huong dan, 97-99 vong quay.
     */
    public static final int POPUP_CUA_AI = 95;
    /** Mot hop / nhan vat / ban do moi chung nay; trong luc do dam vao bien chi hien the nhanh (khong hop). */
    public static final long CHO_HOI_MS = 10000L;
    /** Hop cho -500 qua han nay thi bo (client khong bao nap xong). */
    static final long HAN_CHO_MS = 15000L;
    /** So tay to dam cua ai cua ban do bi chan trong chung nay. */
    static final long TO_DAM_MS = 300000L;

    /** Mot lan hoi: ban do goc, buoc cuoi cua ai, chu hai dong (the nhanh), chu hop (null = chi bao). */
    static final class Hoi {
        final int goc;
        final String maCuoi;
        final String chinh;
        final String phu;
        final String hop;
        final long luc;
        /** > 0: hop "Thách đấu Gym ngay?" (Đồng ý = GymLauDai.thachDauStt(gym)), khong phai hop cua ai. */
        int gym;

        Hoi(final int goc, final String maCuoi, final String chinh, final String phu, final String hop, final long luc) {
            this.goc = goc;
            this.maCuoi = maCuoi;
            this.chinh = chinh;
            this.phu = phu;
            this.hop = hop;
            this.luc = luc;
        }
    }

    /** charDBID -> {ban do goc, luc} lan hien hop gan nhat (chong hien lai khi dam bien lien tuc). */
    private static final ConcurrentHashMap<Integer, long[]> DA_HOI = new ConcurrentHashMap<Integer, long[]>();
    /** charDBID -> hop cho gui sau khi client nap lai map (-500). */
    private static final ConcurrentHashMap<Integer, Hoi> CHO = new ConcurrentHashMap<Integer, Hoi>();
    /** charDBID -> hop dang hien (bam Đồng ý dung mot lan). */
    private static final ConcurrentHashMap<Integer, Hoi> DANG_HOI = new ConcurrentHashMap<Integer, Hoi>();
    /** charDBID -> {ban do goc, luc, id ban do} lan bi chan gan nhat (So tay to dam cua ai can). */
    private static final ConcurrentHashMap<Integer, long[]> VUA_CHAN = new ConcurrentHashMap<Integer, long[]>();

    static long bayGio() {
        return System.currentTimeMillis();
    }

    /** Hoi cho lan chan nay; null = khong phai thieu cua ai (chi thieu cap) / So tay tat / khong tim duoc buoc. */
    static Hoi taoHoi(final Char p, final int toMapId, final long luc) {
        final KhoaBanDo.Luat l = luatCua(toMapId);
        if (l == null || l.ma.isEmpty() || !l.ma.startsWith("CA-") || daXong(p, l) || !com.htbb.pokemon.LauDai.bat()) {
            return null;
        }
        final NhiemVuEngine.BuocCuaAi b = NhiemVuEngine.buocCuaAi(p, l.ma);
        if (b == null) {
            return null;
        }
        final int tt = b.trangThai();
        final String[] d = KhoaBanDo.baoCuaAi(l, capCua(p), tenMap(toMapId), b.so, b.tong,
                KhoaBanDo.moTaBuoc(b.m.mucTieuText, b.m.ten), tt, b.v == null ? 0L : b.v[0], b.m.soLuong, b.loiNhan);
        final String hoi = KhoaBanDo.hoiCuaAi(tt, b.loiNhan);
        return new Hoi(l.mapId, l.ma, d[0] + " " + d[1], d[2], hoi == null ? null : d[0] + "\n" + d[1] + "\n" + d[2] + "\n" + hoi, luc);
    }

    static boolean trongChoHoi(final Char p, final int goc, final long luc) {
        final long[] a = DA_HOI.get(Integer.valueOf(p.charDBID));
        return a != null && a[0] == goc && luc - a[1] < CHO_HOI_MS;
    }

    /**
     * Bao bi chan. Thieu cua ai: hop xac nhan (dieu kien + buoc + tien do + duong So tay, Đồng ý = nhan / xem / nhan
     * thuong) - toi da mot hop / {@link #CHO_HOI_MS} / ban do, giua cac lan chi the nhanh. Con lai (chi thieu cap):
     * the nhanh nhu cu.
     */
    static void baoChan(final Char p, final int toMapId, final String ly, final boolean sauNapMap) {
        final long luc = bayGio();
        final Integer k = Integer.valueOf(p.charDBID);
        Hoi h = null;
        try {
            h = taoHoi(p, toMapId, luc);
        } catch (final Exception e) {
            System.out.println("KhoaBanDoGame.taoHoi: " + e);
        }
        if (h == null) {
            baoChan(p, ly);
            return;
        }
        VUA_CHAN.put(k, new long[]{h.goc, luc, toMapId});
        if (h.hop == null || trongChoHoi(p, h.goc, luc)) {
            the(p, h.chinh, h.phu);
            return;
        }
        if (sauNapMap) {
            CHO.put(k, h);
        } else {
            hoi(p, h, luc);
        }
    }

    static void hoi(final Char p, final Hoi h, final long luc) {
        final Integer k = Integer.valueOf(p.charDBID);
        DA_HOI.put(k, new long[]{h.goc, luc});
        DANG_HOI.put(k, h);
        p.sendMessage(MessageCreator.createMsgPopUp(p.id, POPUP_CUA_AI, h.hop));
    }

    /**
     * sua-tien-do-nv: hop 95 nhac nhan cua ai (MucTieuNhiemVuGame.khiHaQuai - ha quai muc tieu khi chua nhan buoc). Đồng ý
     * -> {@link #khiTraLoi} -> NhiemVuEngine.khiDongYCuaAi(maCuoi) nhu hop ban do khoa (hop moi nhat thang).
     */
    static void hoiNhac(final Char p, final String maCuoi, final String hop) {
        final long luc = bayGio();
        final Hoi h = new Hoi(-1, maCuoi, hop, "", hop, luc);
        DANG_HOI.put(Integer.valueOf(p.charDBID), h);
        p.sendMessage(MessageCreator.createMsgPopUp(p.id, POPUP_CUA_AI, hop));
    }

    /**
     * sua-duong-gym: hop 95 "Thách đấu Trưởng Gym … ngay?" (muc tat Quầy đổi thưởng, chon buoc ha Gym o "Đang làm", nhan
     * buoc, Huỷ o so Nhiem vu). Đồng ý -> {@link #khiTraLoi} -> GymLauDai.thachDauStt (cung cua voi So tay -> Thách đấu Gym).
     */
    static void hoiGym(final Char p, final int stt, final String hop) {
        final Hoi h = new Hoi(-1, "", hop, "", hop, bayGio());
        h.gym = stt;
        DANG_HOI.put(Integer.valueOf(p.charDBID), h);
        p.sendMessage(MessageCreator.createMsgPopUp(p.id, POPUP_CUA_AI, hop));
    }

    /** Goi 12 toMapID -500 (client nap map xong, MapXuLyLenh / MapLienDau): gui hop cua ai dang cho. */
    public static void sauKhiVaoMap(final Char p) {
        if (p == null) {
            return;
        }
        if (baoCuu(p)) {
            CHO.remove(Integer.valueOf(p.charDBID));
            return;
        }
        final Hoi h = CHO.remove(Integer.valueOf(p.charDBID));
        if (h == null) {
            return;
        }
        final long luc = bayGio();
        if (luc - h.luc > HAN_CHO_MS || trongChoHoi(p, h.goc, luc)) {
            return;
        }
        hoi(p, h, luc);
    }

    /** Map.doPopup id {@link #POPUP_CUA_AI}. Moi hop chi dung mot lan (bam hai lan khong nhan hai lan). */
    public static void khiTraLoi(final Char p, final boolean dongY) {
        if (p == null) {
            return;
        }
        final Hoi h = DANG_HOI.remove(Integer.valueOf(p.charDBID));
        if (h == null || !dongY) {
            return;
        }
        if (h.gym > 0) {
            SoTayLauDai.bao(p, GymLauDai.thachDauStt(p, h.gym));
            return;
        }
        synchronized (p.khoaNhiemVu()) {
            NhiemVuEngine.khiDongYCuaAi(p, h.maCuoi);
        }
    }

    /** Ma buoc cua ai can lam tiep cho ban do vua bi chan (So tay to dam); null = khong co. */
    public static String buocVuaChan(final Char p) {
        try {
            final long[] a = p == null ? null : VUA_CHAN.get(Integer.valueOf(p.charDBID));
            if (a == null || bayGio() - a[1] > TO_DAM_MS) {
                return null;
            }
            final KhoaBanDo.Luat l = KhoaBanDo.luat((int) a[0]);
            if (l == null || l.ma.isEmpty() || daXong(p, l)) {
                return null;
            }
            final NhiemVuEngine.BuocCuaAi b = NhiemVuEngine.buocCuaAi(p, l.ma);
            return b == null ? null : b.m.ma;
        } catch (final Exception e) {
            return null;
        }
    }

    /** Duoi nhan hang to dam: " (mở <ten ban do>)". */
    public static String duoiVuaChan(final Char p) {
        final long[] a = p == null ? null : VUA_CHAN.get(Integer.valueOf(p.charDBID));
        final String t = a == null ? "" : tenMap((int) a[2]);
        return t.isEmpty() ? "" : " (mở " + t + ")";
    }

    /** Test: xoa trang thai hop / to dam cua nhan vat. */
    public static void xoaHoi(final int idChar) {
        final Integer k = Integer.valueOf(idChar);
        DA_HOI.remove(k);
        CHO.remove(k);
        DANG_HOI.remove(k);
        VUA_CHAN.remove(k);
        NAP_LAI.remove(k);
        CUU.remove(k);
    }

    /** Test: lui moc hop gan nhat (gia lap het chung cho). */
    public static void luiHoi(final int idChar, final long ms) {
        final long[] a = DA_HOI.get(Integer.valueOf(idChar));
        if (a != null) {
            a[1] -= ms;
        }
    }

    /** The nhanh LOI hai dong (client cu: chu -26 mot cau). */
    static void the(final Char p, final String chinh, final String phu) {
        if (com.htbb.thongbao.ThongBao.coToast(p)) {
            com.htbb.thongbao.ThongBao.gui(p, com.htbb.thongbao.ThongBao.LOI, chinh, phu, com.htbb.thongbao.ThongBao.HINH_KHONG, 0);
        } else {
            bao(p, com.htbb.thongbao.ThongBao.motCau(chinh, phu));
        }
    }

    /**
     * Bao bi chan (khong co hop): client >= 902 = the nhanh ThongBao.LOI (dong 1 dieu kien, dong 2 duong So tay); client
     * cu = chu -26 (bieu ngu tu tat) - KHONG hop thoai chan.
     */
    static void baoChan(final Char p, final String s) {
        final int i = s.indexOf(" " + KhoaBanDo.DUONG_NHAN);
        final String chinh = i > 0 ? s.substring(0, i) : s;
        final String phu = i > 0 ? s.substring(i + 1) : "";
        if (com.htbb.thongbao.ThongBao.coToast(p)) {
            com.htbb.thongbao.ThongBao.gui(p, com.htbb.thongbao.ThongBao.LOI, chinh, phu, com.htbb.thongbao.ThongBao.HINH_KHONG, 0);
        } else {
            bao(p, s);
        }
    }

    /** Ban do vua mo: the vang ThongBao.HIEM (client cu: chu -26). */
    static void baoMo(final Char p, final String s) {
        if (com.htbb.thongbao.ThongBao.coToast(p)) {
            com.htbb.thongbao.ThongBao.gui(p, com.htbb.thongbao.ThongBao.HIEM, s, "", com.htbb.thongbao.ThongBao.HINH_KHONG, 0);
        } else {
            bao(p, s);
        }
    }

    static void bao(final Char p, final String s) {
        try {
            com.htbb.thongbao.ThongBao.rieng(p, s);
        } catch (final Exception ignored) {
        }
    }

    /**
     * Nhan diem den trong menu bay / do (goi -30, chu): khoa -> "Khoá: <nhan>" (client ve hang xam + o khoa theo chu
     * "Khoá"), kem " · AUTO" / " · Không AUTO". Chon diem khoa -> server chan + bao dieu kien (khong tru phi).
     */
    public static String nhanDiem(final Char p, final int mapId, final String nhan) {
        if (!bat()) {
            return nhan;
        }
        final KhoaBanDo.Luat l = luatCua(mapId);
        final boolean khoa = lyDoChan(p, mapId) != null;
        return (khoa ? "Khoá: " : "") + nhan + (l == null || l.tuDong ? " · AUTO" : " · Không AUTO");
    }

    // ------------------------------------------------------------------ tu dong

    /** null = ban do cho tu dong; nguoc lai cau bao. */
    public static String lyDoKhongTuDong(final int mapId) {
        if (!bat()) {
            return null;
        }
        return KhoaBanDo.lyDoTuDong(luatCua(mapId));
    }

    // ------------------------------------------------------------------ nap nhan vat / mien tru

    /**
     * CharNapDuLieu (sau khi chon cho hoi sinh): lan dau -> moc mien = max(cap, cap ban do dang luu) (nhan vat cu truoc
     * khi co khoa ban do). Sau do (sua-ket-tau): dang luu o thuyen ma ben kia khoa, hoac o ban do khoa voi nhan vat ->
     * dua ve lang khoi dau cua vung ({@link #cuuKhiNap}); truoc day nang moc mien len cap ban do do (mo nham ca cua ai).
     */
    public static void khiNap(final Char p) {
        if (p == null || p.charDBID <= 0) {
            return;
        }
        try {
            final KhoaBanDo.Luat luu = luatCua(p.mapID);
            int moc = KhoaBanDo.docMien(p.charDBID);
            if (moc == -2) {
                MIEN.put(Integer.valueOf(p.charDBID), Integer.valueOf(KhoaBanDo.mocMienDau(capCua(p), luu)));
                return; // loi CSDL: mien tru tam, khong ghi, khong dua ai di
            }
            if (moc < 0) {
                moc = KhoaBanDo.mocMienDau(capCua(p), luu);
                KhoaBanDo.ghiMien(p.charDBID, moc, "dau");
            }
            MIEN.put(Integer.valueOf(p.charDBID), Integer.valueOf(moc));
            cuuKhiNap(p);
        } catch (final Exception e) {
            System.out.println("KhoaBanDoGame.khiNap: " + e);
        }
    }

    /** Test: dat moc mien tru. */
    public static void datMien(final int idChar, final int moc) {
        MIEN.put(Integer.valueOf(idChar), Integer.valueOf(moc));
    }

    // ------------------------------------------------------------------ goi -108

    /** Nhom ban do theo (co, yeu cau, ly do tu dong) -> danh sach id (ca ban sao). Ban do mac dinh (mo, cho tu dong) bo. */
    public static LinkedHashMap<String, List<Integer>> nhom(final Char p, final Set<Integer> khoaGoc) {
        final LinkedHashMap<String, List<Integer>> kq = new LinkedHashMap<String, List<Integer>>();
        if (RealController.mapList == null) {
            return kq;
        }
        final List<Integer> ids = new ArrayList<Integer>(RealController.mapList.keySet());
        Collections.sort(ids);
        for (final Integer id : ids) {
            if (id.intValue() < 0) {
                continue;
            }
            final KhoaBanDo.Luat l = luatCua(id.intValue());
            if (l == null) {
                continue;
            }
            final String ly = p.isAdmin ? null : KhoaBanDo.lyDo(l, capCua(p), daXong(p, l), mien(p), tenMap(id.intValue()));
            final int co = (ly != null ? KhoaBanDo.CO_KHOA : 0) | (l.tuDong ? 0 : KhoaBanDo.CO_KHONG_TU_DONG);
            if (co == 0) {
                continue;
            }
            if (ly != null && khoaGoc != null) {
                khoaGoc.add(Integer.valueOf(l.mapId));
            }
            final String k = co + "\u0001" + (ly == null ? "" : ly) + "\u0001" + (l.tuDong ? "" : l.lyDoTuDong);
            List<Integer> ds = kq.get(k);
            if (ds == null) {
                ds = new ArrayList<Integer>();
                kq.put(k, ds);
            }
            ds.add(id);
        }
        return kq;
    }

    /** Goi -108: byte phien ban 1, short n nhom; moi nhom: byte co, UTF yeu cau, UTF ly do tu dong, short k, k x short id. */
    public static Message taoGoi(final LinkedHashMap<String, List<Integer>> nhom) throws java.io.IOException {
        final Message m = new Message(LENH);
        m.dos.writeByte(1);
        m.dos.writeShort(nhom.size());
        for (final java.util.Map.Entry<String, List<Integer>> e : nhom.entrySet()) {
            final String[] a = e.getKey().split("\u0001", -1);
            m.dos.writeByte(Integer.parseInt(a[0]));
            m.dos.writeUTF(a[1]);
            m.dos.writeUTF(a[2]);
            m.dos.writeShort(e.getValue().size());
            for (final Integer id : e.getValue()) {
                m.dos.writeShort(id.intValue());
            }
        }
        return m;
    }

    static boolean clientDoc(final Char p) {
        return p != null && p.session != null && p.session.banClient >= BAN_CLIENT;
    }

    /** Vao game: gui danh sach (khong bao "Đã mở"). */
    public static void guiDanhSach(final Char p) {
        if (p != null) {
            VAN_TAY.remove(Integer.valueOf(p.charDBID)); // phien moi: luon gui
        }
        capNhat(p, false);
    }

    /** Len cap / xong cua ai / admin sua bang: gui lai neu doi, bao ban do vua mo. */
    public static void capNhat(final Char p) {
        capNhat(p, true);
    }

    static void capNhat(final Char p, final boolean baoMo) {
        if (p == null || p.charDBID <= 0) {
            return;
        }
        try {
            final Set<Integer> khoa = new TreeSet<Integer>();
            final LinkedHashMap<String, List<Integer>> n = bat() ? nhom(p, khoa) : new LinkedHashMap<String, List<Integer>>();
            final Integer k = Integer.valueOf(p.charDBID);
            final Set<Integer> truoc = KHOA_TRUOC.put(k, khoa);
            if (baoMo && truoc != null) {
                final List<String> moi = new ArrayList<String>();
                for (final Iterator<Integer> it = truoc.iterator(); it.hasNext(); ) {
                    final Integer g = it.next();
                    if (!khoa.contains(g)) {
                        final String t = tenMap(g.intValue());
                        moi.add(t.isEmpty() ? String.valueOf(g) : t);
                    }
                }
                if (!moi.isEmpty()) {
                    baoMo(p, "Đã mở bản đồ mới: " + String.join(", ", moi) + ".");
                }
            }
            if (!clientDoc(p)) {
                return;
            }
            final int vt = n.hashCode();
            final Integer cu = VAN_TAY.put(k, Integer.valueOf(vt));
            if (cu != null && cu.intValue() == vt) {
                return;
            }
            p.sendMessage(taoGoi(n));
        } catch (final Exception e) {
            System.out.println("KhoaBanDoGame.capNhat: " + e);
        }
    }

    /** Bang luat vua nap lai (admin sua): cap nhat moi nguoi dang choi. */
    static void capNhatTatCa() {
        try {
            for (int i = 0; i < CharManager.instance.vChars.size(); i++) {
                final Char c = CharManager.instance.vChars.elementAt(i);
                if (c != null) {
                    capNhat(c);
                }
            }
        } catch (final Exception e) {
            System.out.println("KhoaBanDoGame.capNhatTatCa: " + e);
        }
    }
}
