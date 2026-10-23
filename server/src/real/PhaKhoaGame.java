package real;

import com.htbb.pokemon.PhaKhoaLogic;
import data.Database;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * [khoa-khi-mac] Muc "Phá khoá trang bị" o Machamp tho ren (NPC loai 2, goi 23 -> menu -30, muc thu 3). Gia: xu (ho hut,
 * dot han) + Da pha khoa (item {@link PhaKhoaLogic#ID_DA_PHA_KHOA}, roi tu hop boss the gioi - data_noi_dung_roi
 * nguon mo_hop). Logic gia / dieu kien o com.htbb.pokemon.PhaKhoaLogic (thuan, test/KiemKhoaKhiMac).
 *
 *   NPC ao {@link #NPC_ID}: menu 0 = danh sach mon khoa do mac trong hanh trang; menu 1 = xac nhan (dong dau ghi
 *   dung gia). Bam xac nhan thi tinh LAI gia va so da: khac luc hoi (mon doi / da pha o noi khac) thi tu choi.
 */
public final class PhaKhoaGame {

    public static final int NPC_ID = -529;
    static final int MENU_CHON = 0;
    static final int MENU_XAC_NHAN = 1;
    /** Vi tri muc "Phá khoá trang bị" trong menu Machamp (RequestNPCInfoHandler case 2). */
    public static final int VI_TRI_MUC_THO_REN = 2;
    public static final String TEN_MUC = "Phá khoá trang bị";
    static final int TOI_DA_DONG = 20;
    static final long NAP_LAI_MS = 5 * 60 * 1000L;

    /** Trang thai dang chon theo charDBID: danh sach id mon da hien, mon dang hoi, gia / so da da hoi. */
    static final class Chon {
        short[] ds = new short[0];
        short idMon = -1;
        long gia = -1L;
        int soDa = -1;
    }

    static final ConcurrentHashMap<Integer, Chon> CHON = new ConcurrentHashMap<Integer, Chon>();
    private static volatile long napLuc = 0L;

    private PhaKhoaGame() {
    }

    static Chon chon(final Char p) {
        Chon c = CHON.get(p.charDBID);
        if (c == null) {
            c = new Chon();
            CHON.put(p.charDBID, c);
        }
        return c;
    }

    /** Nap data_pha_khoa_bac / data_pha_khoa_lan (sql/sua_096_1_logic.sql); loi / thieu bang = giu mac dinh. */
    static void napNeuCan() {
        final long bayGio = System.currentTimeMillis();
        if (bayGio - napLuc < NAP_LAI_MS || Database.instance == null) {
            return;
        }
        napLuc = bayGio;
        try (Connection c = Database.instance.getConnection()) {
            final List<int[]> bac = new ArrayList<int[]>();
            try (PreparedStatement ps = c.prepareStatement(
                    "SELECT color_name, gio_phan_tram, cong_moi_cap_phan_tram FROM data_pha_khoa_bac");
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    bac.add(new int[]{rs.getInt(1), Math.max(0, rs.getInt(2)), Math.max(0, rs.getInt(3))});
                }
            }
            final List<int[]> lan = new ArrayList<int[]>();
            try (PreparedStatement ps = c.prepareStatement("SELECT lan, he_so_phan_tram, so_da FROM data_pha_khoa_lan");
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lan.add(new int[]{rs.getInt(1), Math.max(0, rs.getInt(2)), Math.max(0, rs.getInt(3))});
                }
            }
            PhaKhoaLogic.napBang(bac.toArray(new int[0][]), lan.toArray(new int[0][]));
        } catch (final Exception e) {
            System.out.println("[PHA KHOA] khong nap duoc bang gia (dung mac dinh): " + e);
        }
    }

    static void bao(final Char p, final String s) {
        p.sendMessage(MessageCreator.createServerAlertMessage(s, ""));
    }

    static String ten(final Item it) {
        String t;
        try {
            t = it.getName();
        } catch (final Exception e) {
            t = "Vật phẩm";
        }
        return t + (it.plus > 0 ? " +" + it.plus : "");
    }

    static Item timMon(final Char p, final short id) {
        synchronized (p.iItems) {
            for (final Item it : p.iItems) {
                if (it != null && it.id == id) {
                    return it;
                }
            }
        }
        return null;
    }

    /**
     * Moi duong MAC trang bi goi ham nay ngay sau khi mon vao o mac (MapMuaBan.exchangeItem / exchangeRingItem,
     * Map.exchangeItemAnimal, than khi): mon chua khoa thi khoa do mac va bao mot lan.
     */
    public static void macVao(final Char p, final Item it) {
        if (it == null || !it.khoaKhiMac()) {
            return;
        }
        if (p != null) {
            try {
                // Hop tin (-26), khong phai thong bao nhanh cua DungVatPham: giu nguyen "Đã trang bị ..." cua lenh 29.
                // (client 0.9.8 phan loai chu chay "khoá khi mặc" = tin rieng, khong bieu ngu toan may chu)
                p.sendMessage(MessageCreator.createServerAlertAutoOffMessage(ten(it) + " đã khoá khi mặc: không bán / giao dịch cho người khác được nữa"
                        + " (vẫn bán cho NPC được; phá khoá ở Machamp thợ rèn)."));
            } catch (final Exception ignored) {
            }
        }
    }

    /** Bam muc "Phá khoá trang bị" o Machamp. */
    public static void moMenu(final Char p) {
        if (p == null) {
            return;
        }
        if (!NpcTinhBanDo.oNoiDichVu(p, NpcTinhBanDo.NPC_THO_REN)) {
            bao(p, "Hãy đến gần Machamp thợ rèn để phá khoá trang bị.");
            return;
        }
        napNeuCan();
        final List<String> dong = new ArrayList<String>();
        final List<Short> ids = new ArrayList<Short>();
        synchronized (p.iItems) {
            for (final Item it : p.iItems) {
                if (it != null && it.laKhoaDoMac() && !it.isSelling && dong.size() < TOI_DA_DONG) {
                    dong.add(ten(it) + " (phá khoá " + it.soLanPhaKhoa + "/" + PhaKhoaLogic.lanToiDa() + ")");
                    ids.add(it.id);
                }
            }
        }
        if (dong.isEmpty()) {
            bao(p, "Hành trang không có trang bị nào khoá do đã mặc. Tháo trang bị ra hành trang rồi quay lại."
                    + " Đồ khoá vì lý do khác (mua bằng lượng khoá, quà nhiệm vụ / sự kiện) không phá khoá được.");
            return;
        }
        final Chon c = chon(p);
        c.ds = new short[ids.size()];
        for (int i = 0; i < ids.size(); i++) {
            c.ds[i] = ids.get(i).shortValue();
        }
        c.idMon = -1;
        p.sendMessage(MessageCreator.createMsgMenu(dong.toArray(new String[0]), NPC_ID, MENU_CHON));
    }

    /** MapDoMenu: idNpc == NPC_ID. */
    public static void doMenu(final Char p, final int idMenu, final int idOptionMenu) {
        if (p == null) {
            return;
        }
        final Chon c = chon(p);
        if (idMenu == MENU_CHON) {
            if (idOptionMenu < 0 || idOptionMenu >= c.ds.length) {
                return;
            }
            hoiXacNhan(p, c, c.ds[idOptionMenu]);
            return;
        }
        if (idMenu == MENU_XAC_NHAN) {
            if (idOptionMenu == 0) {
                thucHien(p, c);
            } else {
                c.idMon = -1;
            }
        }
    }

    static void hoiXacNhan(final Char p, final Chon c, final short idMon) {
        final Item it = timMon(p, idMon);
        if (it == null) {
            bao(p, "Không tìm thấy vật phẩm trong hành trang.");
            return;
        }
        final String loi = PhaKhoaLogic.lyDoKhongDuoc(it.lock, it.daMac, it.soLanPhaKhoa, it.isSelling);
        if (loi != null) {
            bao(p, loi);
            return;
        }
        c.idMon = idMon;
        c.gia = PhaKhoaLogic.giaXu(it.level, it.colorName, it.plus, it.soLanPhaKhoa);
        c.soDa = PhaKhoaLogic.soDa(it.soLanPhaKhoa);
        final int lan = it.soLanPhaKhoa + 1;
        p.sendMessage(MessageCreator.createMsgMenu(new String[]{
            "Phá khoá " + ten(it) + " (lần " + lan + "/" + PhaKhoaLogic.lanToiDa() + "): "
                    + PhaKhoaLogic.dinhDang(c.gia) + " xu + " + c.soDa + " Đá phá khoá",
            "Huỷ",
        }, NPC_ID, MENU_XAC_NHAN));
    }

    static void thucHien(final Char p, final Chon c) {
        if (c.idMon < 0) {
            return;
        }
        final short idMon = c.idMon;
        c.idMon = -1;
        if (!NpcTinhBanDo.oNoiDichVu(p, NpcTinhBanDo.NPC_THO_REN)) {
            bao(p, "Hãy đến gần Machamp thợ rèn để phá khoá trang bị.");
            return;
        }
        final String ketQua = phaKhoa(p, idMon, c.gia, c.soDa);
        bao(p, ketQua);
        if (ketQua.startsWith("Đã phá khoá")) {
            p.sendMessage(MessageCreator.createCharInventoryMessage(p, 1));
            p.sendMessage(MessageCreator.createCharInventoryMessage(p, 0));
            try {
                Database.instance.saveCharAuto(p);
            } catch (final Exception ignored) {
            }
        }
    }

    /**
     * Pha khoa mon {@code idMon} trong hanh trang voi gia / so da DA HOI. Tat ca hoac khong: kiem lai dieu kien,
     * gia, so da, xu duoi khoa hanh trang; tru xu (dot, khong cong cho ai) + da roi mo khoa mon.
     * @return thong bao cho nguoi choi ("Đã phá khoá ..." khi thanh cong)
     */
    public static String phaKhoa(final Char p, final short idMon, final long giaDaHoi, final int soDaDaHoi) {
        synchronized (p.iItems) {
            final Item it = timMon(p, idMon);
            if (it == null) {
                return "Không tìm thấy vật phẩm trong hành trang.";
            }
            final String loi = PhaKhoaLogic.lyDoKhongDuoc(it.lock, it.daMac, it.soLanPhaKhoa, it.isSelling);
            if (loi != null) {
                return loi;
            }
            final long gia = PhaKhoaLogic.giaXu(it.level, it.colorName, it.plus, it.soLanPhaKhoa);
            final int soDa = PhaKhoaLogic.soDa(it.soLanPhaKhoa);
            if (gia != giaDaHoi || soDa != soDaDaHoi) {
                return "Giá phá khoá đã thay đổi, hãy chọn lại.";
            }
            if (VatPhamDungLauDai.demItem(p, PhaKhoaLogic.ID_DA_PHA_KHOA) < soDa) {
                return "Cần " + soDa + " Đá phá khoá (rơi từ hộp đột kích boss thế giới).";
            }
            final String truoc = ten(it) + " | " + it.getDBInfo();
            if (!p.truXuNeuDu(gia, true, "pha khoa trang bi " + it.tempateID)) {
                return "Không đủ " + PhaKhoaLogic.dinhDang(gia) + " xu.";
            }
            if (!VatPhamDungLauDai.truItem(p, PhaKhoaLogic.ID_DA_PHA_KHOA, soDa)) {
                // Khong the xay ra (da dem duoi cung khoa hanh trang) - tra lai xu cho chac.
                p.addXu(gia, "hoan pha khoa trang bi", Char.XU_HOAN);
                return "Không đủ Đá phá khoá.";
            }
            it.lock = 0;
            it.daMac = 0;
            it.soLanPhaKhoa = (byte) (it.soLanPhaKhoa + 1);
            try {
                Database.instance.saveLogThongKeChiTieu("Pha khoa trang bi_xu", it.tempateID, 1, gia, "xu");
                Database.instance.saveLogThongKeChiTieu("Pha khoa trang bi_da", PhaKhoaLogic.ID_DA_PHA_KHOA, soDa, 0L, "da");
                Database.instance.saveOrtherLog("tob_log_other_item", p.charname,
                        "pha khoa " + truoc + " | " + gia + " xu + " + soDa + " da | lan " + it.soLanPhaKhoa, "phakhoa");
            } catch (final Exception ignored) {
            }
            return "Đã phá khoá " + ten(it) + " (" + it.soLanPhaKhoa + "/" + PhaKhoaLogic.lanToiDa()
                    + "). Mặc lại sẽ khoá lại.";
        }
    }
}
