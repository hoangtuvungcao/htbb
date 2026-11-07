package real;

import io.Message;

/**
 * Xe hang van tieu (Char.monster, MonsterVantieu) di cung nguoi choi khi doi ban do (sua-xe-lac-map, bao loi 07/11/2026).
 * <p>
 * Bao loi (ruby, cap 72, 07/11 ~09:24): van tieu di tau (Bến tàu 106 -&gt; Thuyền 107 -&gt; New Bark 10 -&gt; ... -&gt; Đường
 * Victory 118), toi Dịch trạm bao "Không thể trả hàng do khác map". Nguyen nhan: xe chi di theo o DUNG MOT duong - di bo qua
 * cong (MapXuLyLenh.doChangeMap) - va ca o do chi khi ban do dich la ban goc (mapId == mapIDLoadMap, &lt; 301) va ban do
 * nguon khong phai ban sao theo khu. Moi duong khac (Map.move2Map: len tau CharVongLap, xuong tau KhoaBanDoGame.xuongThuyen,
 * tau dua ve ben) khong dua xe -&gt; xe o lai ben tau; di bo trong ban sao khu (321.., 501.., 2421.., 2441..) cung bo xe.
 * <p>
 * Nay: mot luat chung cho moi duong hop le (di bo qua cong, len / xuong tau, tau dua ve ben): xe "di cung" nguoi choi
 * ({@link #diCung}: cung ban do, khu, vung, trong {@link #TAM} px) va ban do dich cho xe vao ({@link #choXeVao}: ban do thuong
 * / lang / thuyen / ban sao khu; KHONG nha, Gym, hang, dau truong, phong rieng) thi xe sang cung, dat canh nguoi choi
 * ({@link #keoXe}). Duong dich chuyen tat (ve, xa phu / Pidgeot bay, Arcanine, Lapras, Hoa Tieu...) tu choi TRUOC khi di
 * ({@link #chan}). Xe da lac (con xe, nguoi choi roi ban do luc dang canh xe - {@link MonsterVantieu#ganChu}): toi cho giao
 * (Dịch trạm / Kecleon) xe duoc keo ve (TraHang.xeTaiCho).
 */
public final class XeHangChuyenMap {

    /** Xe trong tam nay (px moi truc) quanh nguoi choi luc doi ban do thi di cung (xe dung cach 16 px, di 30 px / 300 ms). */
    public static final int TAM = 160;

    private XeHangChuyenMap() {
    }

    /** Xe hang con chay (chua giao / het gio / bi pha) cua p, khong co: null. */
    public static MonsterVantieu xe(final Char p) {
        final MonsterVantieu m = p == null ? null : p.monster;
        return m != null && !m.isFinish() ? m : null;
    }

    /** Xe dang o cung ban do (cung doi tuong Map), khu, vung voi p. */
    public static boolean cungCho(final Char p, final MonsterVantieu m) {
        return m != null && p.map != null && m.map == p.map && m.region == p.region && m.inCountry == p.inCountry;
    }

    /** Xe dang di cung p (doi ban do thi xe sang cung). */
    public static boolean diCung(final Char p) {
        final MonsterVantieu m = xe(p);
        return cungCho(p, m) && Map.inRangeActor((Actor) p, (Actor) m, TAM);
    }

    /** Ban do xe vao duoc: ban do thuong / lang / thuyen / ban sao khu. Khong: nha (offline), Gym, hang, dau truong... */
    public static boolean choXeVao(final Map m) {
        return m != null && !m.isMapOffline && m.mapId >= 0 && (m.isMapTrain() || Map.isMapLang(m));
    }

    /**
     * Dua xe sang ban do / vi tri hien tai cua p (goi SAU khi p da vao ban do moi va co toa do moi): bo xe khoi ban do cu
     * (bao nguoi choi o do), dat canh p, xoa vet (vet cu vo nghia o ban do moi).
     */
    public static void keoXe(final Char p) {
        final MonsterVantieu m = xe(p);
        if (m == null || p.map == null) {
            return;
        }
        final Map cu = m.map;
        final int khuCu = m.region;
        final int vungCu = m.inCountry;
        if (cu != null && (cu != p.map || khuCu != p.region || vungCu != p.inCountry)) {
            try {
                m.removeOutMap();
            } catch (final Throwable ignored) {
            }
            try {
                final Message bo = new Message(90);
                bo.dos.writeShort(m.id);
                bo.dos.writeByte(m.cat);
                cu.sendAllPlayer(bo, vungCu);
            } catch (final Throwable ignored) {
            }
        }
        m.map = p.map;
        m.region = p.region;
        m.inCountry = p.inCountry;
        m.x = p.x;
        m.y = p.y;
        m.vet.xoa();
        m.target = p;
        m.ganChu = true;
        try {
            m.doSendTonearChar();
        } catch (final Throwable ignored) {
        }
    }

    /**
     * Sau khi p doi ban do: {@code diCung} = xe di cung truoc khi doi (tinh TRUOC playerExit). Ban do dich cho xe vao: keo
     * xe; khong: bao xe dang doi o dau. Xe khong di cung (bi bo xa) ma vua roi ban do cua xe: bao.
     */
    public static void sauDoiMap(final Char p, final Map tu, final boolean diCung) {
        final MonsterVantieu m = xe(p);
        if (m == null || p.map == null || p.map == tu) {
            return; // khong doi ban do (bi chan / o lai cho cu)
        }
        if (cungCho(p, m)) {
            return; // da cung ban do (vd quay lai)
        }
        if (diCung && choXeVao(p.map)) {
            keoXe(p);
            ghi(p, "xe sang " + p.mapID);
            return;
        }
        if (m.map != tu || p.map.isMapOffline) {
            return; // xe khong o ban do vua roi / vao nha (xe doi ngoai cua - HoTongNhiemVu.BAO_XE_NGOAI_NHA)
        }
        bao(p, (diCung ? "Xe hàng không vào được " + tenMap(p.mapID) + ", đang đợi ở " : "Xe hàng ở xa nên không đi cùng bạn, đang đợi ở ")
                + noiXe(m) + ".");
        ghi(p, "xe o lai " + m.map.mapId + " (" + m.x / 16 + "," + m.y / 16 + ") nguoi choi sang " + p.mapID + (diCung ? " (ban do cam xe)" : " (xe o xa)"));
    }

    /** move2Map cua duong hop le co xe di cung (len / xuong tau, tau dua ve ben). */
    public static void move2MapCungXe(final Map tu, final Char p, final int x, final int y, final int den, final int vung) {
        final boolean di = diCung(p);
        final Map cu = p.map;
        tu.move2Map(p, x, y, den, vung);
        sauDoiMap(p, cu, di);
    }

    /**
     * Duong dich chuyen tat (ve, bay, Arcanine, Lapras, Hoa Tieu...): dang van tieu thi bao va tra true (khong di). Goi TRUOC
     * khi tru tien / ve.
     */
    public static boolean chan(final Char p, final String cach) {
        if (xe(p) == null) {
            return false;
        }
        bao(p, cauChan(cach));
        return true;
    }

    public static String cauChan(final String cach) {
        return "Đang giao hàng, không thể dùng " + cach + " - hãy đi bộ / đi tàu cùng xe hàng.";
    }

    /** Cap nhat {@link MonsterVantieu#ganChu} moi nhip nguoi choi (CharVongLap): chi khi cung ban do. */
    public static void capNhatGanChu(final Char p) {
        final MonsterVantieu m = xe(p);
        if (cungCho(p, m)) {
            m.ganChu = Map.inRangeActor((Actor) p, (Actor) m, TAM);
        }
    }

    /** "Tên bản đồ (x,y)" noi xe dang o. */
    public static String noiXe(final MonsterVantieu m) {
        return tenMap(m.map == null ? -1 : m.map.mapId) + " (" + m.x / 16 + "," + m.y / 16 + ")";
    }

    static String tenMap(final int id) {
        String t = "";
        try {
            t = KhoaBanDoGame.tenMap(id);
        } catch (final Throwable ignored) {
        }
        if (t == null || t.isEmpty()) {
            try {
                t = Map.getNameMap(id);
            } catch (final Throwable ignored) {
            }
        }
        return t == null || t.isEmpty() ? "bản đồ " + id : t;
    }

    static void bao(final Char p, final String s) {
        try {
            p.sendMessage(MessageCreator.createServerAlertMessage(s, ""));
        } catch (final Throwable ignored) {
        }
    }

    static void ghi(final Char p, final String s) {
        try {
            System.out.println("[VAN TIEU] " + p.charname + " " + s);
        } catch (final Throwable ignored) {
        }
    }
}
