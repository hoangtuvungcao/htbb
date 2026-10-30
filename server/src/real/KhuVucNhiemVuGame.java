package real;

import com.htbb.pokemon.KhuVucNhiemVu;
import com.htbb.pokemon.KhuVucNhiemVu.Vung;
import data.Database;
import data.InfoQuestTemplate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * BUOC NHIEM VU DIA DIEM (nhanh sua-tuyen) - lop noi game cua com.htbb.pokemon.KhuVucNhiemVu.
 *
 * <pre>
 *  Du lieu: data_nhiem_vu_khu_vuc (sql/htbb.sql khoi sua_tuyen_nhiem_vu.sql, sinh boi tools/pokemon/sinh_sua_tuyen.py), theo ma nhiem vu
 *  (data_quest_lau_dai.ma = InfoQuestTemplate.ma). Chi buoc noi chuyen (type 3) cua chuoi chinh / phu (-64) co vung.
 *  Char.xuLyNhiemVu (tra -64)  -> {@link #chuaToi}: chua dung trong vung -> nhan buoc (dang lam), bao chi duong, KHONG tra.
 *  CharCapNhat.doPlayerMove    -> {@link #khiDiChuyen}: chi nhan vat co buoc chinh / phu DANG LAM kieu noi chuyen co vung
 *                                 (vai phep so sanh truoc khi tra bang); vao vung -> xong (hoac "da toi" neu noi_chuyen_sau).
 *  MessageCreator -64 nhom 2    -> {@link #moTaDangLam}: mo ta ngan + chi duong (o, huong) - client 0.9.7 hien san trong So
 *                                 nhiem vu; "da toi" (noi_chuyen_sau) dung trang thai du -> dau nhiem vu tren NPC tra nhu cu.
 * </pre>
 */
public final class KhuVucNhiemVuGame {

    /** ma nhiem vu -> cac vung dich (vao vung nao cung duoc). */
    static volatile HashMap<String, List<Vung>> KHU = new HashMap<String, List<Vung>>();

    private KhuVucNhiemVuGame() {
    }

    /** NoiDungLauDai.sauKhiNapTemplate: nap data_nhiem_vu_khu_vuc (bat = 1). */
    public static void nap() {
        final HashMap<String, List<Vung>> m = new HashMap<String, List<Vung>>();
        for (final Object[] d : data.DatabaseLauDai.napKhuVucNhiemVu()) {
            try {
                final String ma = String.valueOf(d[0]);
                final Vung v = new Vung(ma, so(d[1]), so(d[2]), so(d[3]), so(d[4]), so(d[5]), so(d[6]) == 1,
                        d[7] == null ? "" : String.valueOf(d[7]));
                m.computeIfAbsent(ma, k -> new ArrayList<Vung>()).add(v);
            } catch (final Exception e) {
                System.out.println("KhuVucNhiemVuGame.nap: " + e);
            }
        }
        KHU = m;
        System.out.println("[LAU DAI] buoc nhiem vu dia diem: " + m.size() + " buoc co vung dich.");
    }

    /** Test / cong cu: dat bang vung truc tiep. */
    public static void dat(final HashMap<String, List<Vung>> m) {
        KHU = m == null ? new HashMap<String, List<Vung>>() : m;
    }

    static int so(final Object o) {
        return o instanceof Number ? ((Number) o).intValue() : Integer.parseInt(String.valueOf(o).trim());
    }

    /** Cac vung dich cua buoc (chi buoc noi chuyen type 3 co ma); null = buoc thuong. */
    public static List<Vung> cua(final InfoQuestTemplate info) {
        if (info == null || info.type != 3 || info.ma == null || info.ma.isEmpty()) {
            return null;
        }
        final List<Vung> ds = KHU.get(info.ma);
        return ds == null || ds.isEmpty() ? null : Collections.unmodifiableList(ds);
    }

    static int ox(final Char p) {
        return p.x / 16;
    }

    static int oy(final Char p) {
        return p.y / 16;
    }

    /** Nhan vat dang dung trong vung dich cua buoc. */
    public static boolean dangTrong(final Char p, final List<Vung> ds) {
        return KhuVucNhiemVu.trong(ds, NpcTinhBanDo.banDoNap(p), ox(p), oy(p));
    }

    /**
     * Char (tra -64 buoc noi chuyen): buoc co vung dich, CHUA toi (trang thai khac du) va dang dung ngoai vung -> true
     * (khong tra; Char nhan buoc / giu dang lam roi goi {@link #baoChuaToi}).
     */
    public static boolean chuaToi(final Char p, final NewCharQuest q, final InfoQuestTemplate info) {
        final List<Vung> ds = cua(info);
        return ds != null && q != null && q.state_quest != NewCharQuest.FINISH && q.state_quest != NewCharQuest.DONE && !dangTrong(p, ds);
    }

    /** Client ve dau sao / nut "Đi tới dấu sao" (doc duoi thu hai -64) tu ban nay; client cu chi nhan chu (khong nhac dau sao). */
    public static final int BAN_DAU_SAO = 922;

    static boolean coDauSao(final Char p) {
        return p != null && p.session != null && p.session.banClient >= BAN_DAU_SAO;
    }

    /** Ten noi cua vung (ten_noi CSDL, khong co thi ten ban do). */
    static String tenNoi(final Vung v) {
        return v.ten.isEmpty() ? TenBanDoMayChu.ten(v.map) : v.ten;
    }

    /**
     * Chu chi duong cua buoc cho nhan vat (o, huong, ban do) + vi tri bang loi ("góc trên bên phải bản đồ") + loi di
     * cho khu xa khong co cong (Arcanine gac cong) - bao loi 29/10/2026: nguoi choi khong biet o 53,19 la dau.
     */
    static String chiDuong(final Char p, final List<Vung> ds) {
        final Vung d = ds.get(0);
        final int bd = NpcTinhBanDo.banDoNap(p);
        final String goc = KhuVucNhiemVu.chiDuong(ds, bd, ox(p), oy(p), tenNoi(d));
        if (KhuVucNhiemVu.trong(ds, bd, ox(p), oy(p))) {
            return goc;
        }
        final Vung g = KhuVucNhiemVu.ganNhat(ds, bd, ox(p), oy(p));
        final Vung v = g != null ? g : d;
        final StringBuilder sb = new StringBuilder(goc);
        final int[] kt = kichThuoc(v.map);
        final String vt = KhuVucNhiemVu.viTri(v, kt[0], kt[1]);
        if (!vt.isEmpty()) {
            final String tb = TenBanDoMayChu.ten(v.map);
            sb.append(", ").append(vt).append(" bản đồ ").append(tb == null || tb.isEmpty() ? tenNoi(v) : tb);
        }
        if (g == null) {
            boolean khuXa = true;
            for (final Vung x : ds) {
                khuXa &= NpcDichVu.laKhuXa(x.map);
            }
            if (khuXa) {
                sb.append(" - nhờ Arcanine gác cổng ở làng dẫn đường");
            }
        }
        if (!KhuVucNhiemVu.laCaBanDo(v) && coDauSao(p)) {
            sb.append(" - theo dấu sao vàng trên bản đồ");
        }
        return sb.toString();
    }

    private static final java.util.concurrent.ConcurrentHashMap<Integer, int[]> KICH_THUOC = new java.util.concurrent.ConcurrentHashMap<Integer, int[]>();

    /** Kich thuoc (o) cua ban do nap {@code map} doc tu cMap; {0, 0} = khong doc duoc. */
    static int[] kichThuoc(final int map) {
        int[] k = KICH_THUOC.get(Integer.valueOf(map));
        if (k == null) {
            k = new int[]{0, 0};
            try {
                final com.htbb.pokemon.DinhDangBanDo b = com.htbb.pokemon.DinhDangBanDo.doc(
                        java.nio.file.Files.readAllBytes(new java.io.File("cMap/" + map).toPath()));
                k = new int[]{b.w, b.h};
            } catch (final Exception ignored) {
                // test / thieu tep: khong co vi tri bang loi
            }
            KICH_THUOC.put(Integer.valueOf(map), k);
        }
        return k;
    }

    /**
     * Diem dich cho client (duoi thu hai goi -64, client &gt;= 0.9.23 ve dau ★ tren the gioi / ban do nho / ban do to,
     * mui ten mep man, nut "Đi tới dấu sao"): {ban do nap, o tam x, o tam y, nua rong, nua cao, ca ban do 0/1}; null =
     * buoc khong co vung / khong dang lam. Vung tren ban do dang dung uu tien (gan nhat), khong thi vung dau.
     */
    public static int[] dich(final Char p, final NewCharQuest q, final InfoQuestTemplate info) {
        final List<Vung> ds = cua(info);
        if (ds == null || q == null || q.state_quest != NewCharQuest.WORKING) {
            return null;
        }
        Vung v = p == null ? null : KhuVucNhiemVu.ganNhat(ds, NpcTinhBanDo.banDoNap(p), ox(p), oy(p));
        if (v == null) {
            v = ds.get(0);
        }
        return new int[]{v.map, v.tamX(), v.tamY(), (v.x2 - v.x1) / 2, (v.y2 - v.y1) / 2, KhuVucNhiemVu.laCaBanDo(v) ? 1 : 0};
    }

    /** Ten noi cua diem {@link #dich} (chu nut / the client). */
    public static String tenDich(final Char p, final InfoQuestTemplate info) {
        final List<Vung> ds = cua(info);
        if (ds == null) {
            return "";
        }
        Vung v = p == null ? null : KhuVucNhiemVu.ganNhat(ds, NpcTinhBanDo.banDoNap(p), ox(p), oy(p));
        if (v == null) {
            v = ds.get(0);
        }
        final int[] kt = kichThuoc(v.map);
        final String vt = KhuVucNhiemVu.viTri(v, kt[0], kt[1]);
        return tenNoi(v) + (vt.isEmpty() ? "" : " (" + vt + ")");
    }

    /** Bao nguoi choi: da nhan buoc dia diem, can toi dau. */
    public static void baoChuaToi(final Char p, final InfoQuestTemplate info) {
        final List<Vung> ds = cua(info);
        if (ds == null) {
            return;
        }
        p.khuVucDaBao = NpcTinhBanDo.banDoNap(p);
        final String muc = info.shortContent == null || info.shortContent.isEmpty() ? info.name : info.shortContent;
        p.sendMessage(MessageCreator.createServerAlertMessage(muc + ": " + chiDuong(p, ds) + "."
                + (KhuVucNhiemVu.noiChuyenSau(ds) ? " Tới nơi rồi quay lại nói chuyện để hoàn thành." : " Tới nơi là hoàn thành.")
                + (coDauSao(p) ? " Đứng gần dấu sao vàng (trong vài ô) là được; mở Nhiệm vụ, bấm \"Đi tới dấu sao\" để tự tìm đường."
                        : " Đứng trong vài ô quanh điểm đó là được."), ""));
    }

    /** MessageCreator -64 nhom 2 (dang lam): mo ta ngan + chi duong cho buoc dia diem dang lam. */
    public static String moTaDangLam(final Char p, final NewCharQuest q, final InfoQuestTemplate info) {
        final String goc = info == null || info.shortContent == null ? "" : info.shortContent;
        try {
            final List<Vung> ds = cua(info);
            if (ds == null || q == null || p == null || q.state_quest != NewCharQuest.WORKING) {
                return goc;
            }
            return goc + " (" + chiDuong(p, ds) + ")";
        } catch (final Exception e) {
            return goc;
        }
    }

    /**
     * NPC tra ghi vao goi -64 (byte). Buoc noi chuyen co npcres = 32000 (ghi byte = 0 -> client 0.9.7 hien "Báo lại:
     * Chansey" + dau nhiem vu tren dau Chansey). Buoc dia diem: dang lam (nhom 2) -> -1 (client: "NPC nhiệm vụ", khong dau
     * nham); da toi (nhom 1) -> NPC giao (noi chuyen de hoan thanh). Buoc khac: nhu cu.
     */
    public static int npcTra(final InfoQuestTemplate info, final int nhom) {
        if (info == null) {
            return 0;
        }
        if (cua(info) == null && HanhDongNhiemVuGame.mau(info) == null) {
            return info.idNpcRes; // sua-buoc-hanh-dong: buoc hanh dong that cung luat NPC nhu buoc dia diem
        }
        if (nhom == 2) {
            return -1;
        }
        return info.idNpcRes >= 32000 || info.idNpcRes < 0 ? info.idNpcRcv : info.idNpcRes;
    }

    /** CharCapNhat.doPlayerMove: kiem buoc dia diem cua nhiem vu chinh / phu dang lam. */
    public static void khiDiChuyen(final Char p) {
        if (p == null || KHU.isEmpty()) {
            return;
        }
        try {
            kiem(p, p.mainQuest, (byte) 0);
            kiem(p, p.subQuest, (byte) 1);
        } catch (final Exception e) {
            System.out.println("KhuVucNhiemVuGame.khiDiChuyen: " + e);
        }
    }

    /**
     * Vua doi ban do (MapXuLyLenh.doChangeMap qua cua, Map.move2Map - Arcanine gac cong / dich chuyen): kiem buoc dia diem
     * NGAY, khong cho goi di chuyen. Client (client_jar GameService.moveChar, client_pc_ios GameService.moveChar) KHONG
     * gui goi 4 khi dang trong nha (Tilemap.isOfflineMap) -> buoc dich la ca ngoi nha (C05.2 "Bảo tàng thiên thạch":
     * nha 100 / 102 / 72 / 82) truoc day khong bao gio xong voi nguoi choi that (bao loi 30/10/2026 "vẫn ko làm đc nv nhà
     * pewter", J2ME; bot di vai buoc trong nha nen khong thay).
     */
    public static void sauKhiVaoBanDo(final Char p) {
        try {
            NhiemVuEngine.khiDiChuyen(p); // So tay: den_noi / dung_toa_do trong nha
        } catch (final Exception e) {
            System.out.println("KhuVucNhiemVuGame.sauKhiVaoBanDo (so tay): " + e);
        }
        khiDiChuyen(p);
    }

    static void kiem(final Char p, final NewCharQuest q, final byte ms) {
        if (q == null || q.id_quest < 0 || q.state_quest != NewCharQuest.WORKING) {
            return; // nhanh nhat: phan lon nhan vat dung o day
        }
        final InfoQuestTemplate info = q.getTemplate();
        final List<Vung> ds = cua(info);
        if (ds == null) {
            return;
        }
        final int bd = NpcTinhBanDo.banDoNap(p);
        if (!KhuVucNhiemVu.trong(ds, bd, ox(p), oy(p))) {
            if (bd != p.khuVucDaBao && KhuVucNhiemVu.ganNhat(ds, bd, ox(p), oy(p)) != null) {
                // vua vao ban do dich: chi duong mot lan + lam moi danh sach dang lam (dong tien do)
                p.khuVucDaBao = bd;
                LauDaiNhanVat.bao(p, "Điểm nhiệm vụ " + info.name + ": " + chiDuong(p, ds) + ".");
                MessageCreator.createMsgCharQuest(p, 2);
            }
            return;
        }
        toi(p, q, info, ds, ms);
    }


    /** Vao vung dich. */
    static void toi(final Char p, final NewCharQuest q, final InfoQuestTemplate info, final List<Vung> ds, final byte ms) {
        synchronized (p.khoaNhiemVu()) {
            if (q != (ms == 0 ? p.mainQuest : p.subQuest) || q.state_quest != NewCharQuest.WORKING) {
                return;
            }
            q.state_quest = NewCharQuest.FINISH; // "da toi": luu cung trang thai du (vao lai game van con)
        }
        p.khuVucDaBao = -1;
        if (KhuVucNhiemVu.noiChuyenSau(ds)) {
            try {
                Database.instance.saveCharQuest(p.charDBID, p.getInfoQuestSave());
            } catch (final Exception e) {
                System.out.println("KhuVucNhiemVuGame.toi luu: " + e);
            }
            try {
                p.sendInfoQuest();
                p.sendMessage(MessageCreator.createServerAlertMessage("Đã tới nơi (" + info.name + "). Quay lại nói chuyện để hoàn thành.", ""));
            } catch (final Exception e) {
                System.out.println("KhuVucNhiemVuGame.toi gui: " + e);
            }
            return;
        }
        final short id = q.id_quest;
        p.OnQuest((byte) 1, id, ms); // tra nhu nguoi choi bam tra: thuong, sang buoc sau, luu
        final NewCharQuest sau = ms == 0 ? p.mainQuest : p.subQuest;
        final boolean daTra = sau != q || sau.id_quest != id || sau.state_quest == NewCharQuest.DONE;
        final String xong = info.infofinish == null ? "" : info.infofinish.replace("|", "\n");
        if (daTra && !xong.isEmpty()) {
            p.sendMessage(MessageCreator.createServerAlertMessage(xong, ""));
        }
    }
}
