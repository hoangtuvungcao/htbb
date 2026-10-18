package real;

import data.Database;
import data.DatabaseGiftCode;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * PHAT QUA DUA TOP tu trang quan tri web (yeu cau nguoi dung 19/09/2026).
 *
 * VI SAO CAN. Truoc day "dua top" chi co mot co bat/tat rieng (SuKien.DUA_TOP_HOA_LU)
 * khong gan voi phan thuong nao ca - admin muon phat thuong cho top phai lam thu cong
 * (vd goi lenh chat cong tung nguoi). Tinh nang nay cho admin: (1) nhap ten NHAN VAT
 * cho tung hang 1..10, (2) chon vat pham se phat cho hang do (dung LAI dung mot bo chon
 * + cu phap chuoi phan thuong cua trang Giftcode (HTBB-Web /admin/giftcode), xem HTBB-Web shared/utils/thuong.ts),
 * (3) nhan vat duoc xep hang se thay mot muc "Nhan thuong đua top" o NPC Tong Quan
 * (npcType -10) - CHI nguoi co ten trong bang nay va CHUA nhan moi thay muc do.
 *
 * BANG `tob_top_thuong`: moi dong la MOT phan thuong cho MOT nhan vat o MOT dot dua
 * top - khong unique theo vi_tri, vi dua top lap lai nhieu dot (moi dot admin tao dong
 * moi, dong dot truoc da nhan van giu lai de tra cuu lich su, giong cach giftcode_log
 * giu lai sau khi giftcode bi xoa).
 *
 * PHAN THUONG CHO NGUOI CHOI TU CHON (them 19/09/2026, cot `item_chon`): mot so loai
 * qua (mat na, phi phong khang, thoi trang...) admin muon de CHINH NGUOI CHOI chon,
 * khong ep san mot mon. Cu phap item_chon: "nhan1=chuoi1|nhan2=chuoi2|..." - moi doan
 * la MOT lua chon, "chuoi" theo dung cu phap phan thuong binh thuong (ITEM:.., MATNA:..,
 * PPRK:..). Nguoi choi bam "Nhan thuong đua top" thi CAC DONG CHI CO item (khong co
 * item_chon) duoc cong NGAY nhu cu; dong DAU TIEN co item_chon se hien menu de ho tu
 * chon, chon xong moi cong (ca item_chon da chon LAN item co dinh cua dong do neu co)
 * va danh dau da nhan. Neu co NHIEU dong item_chon, xu ly LAN LUOT tung dong mot.
 *
 * MENU 2 BUOC (them 19/09/2026, yeu cau nguoi dung "chon loai mat nạ truoc, roi moi
 * chon tiep cac dong chi so"): nhan trong item_chon co the la "ten nhom::nhan con"
 * (vd bo chon trang Dua top (HTBB-Web /admin/dua-top) sinh "Mặt nạ bí ngô::Thuộc tính Lân (+50%)") - cac
 * doan CUNG ten nhom duoc GOM LAI thanh mot NHOM CAP 1 (vd 5 mau mat na), moi nhom co
 * danh sach NHAN CON cap 2 (vd 5 hieu ung). Nguoi choi chon nhom (cap 1) TRUOC, roi
 * moi chon nhan con (cap 2) trong nhom do; nhom chi co DUNG 1 nhan con (kieu nhan
 * "phang" khong co "::", vd tu nut "Thêm" don le) thi ap dung luon khi chon o cap 1,
 * khong hoi them. Xem moMenuChonThuong/doChonThuong.
 *
 * AP DUNG PHAN THUONG: dung lai DatabaseGiftCode.applyItemString() - CHINH XAC cung bo
 * ~60 loai phan thuong (ITEM/GEM/PT/TT/PET/...) ma giftcode dang dung, khong viet lai.
 *
 * KHONG can vong lap rieng (khac LenhWeb): menu chi hien/an tai THOI DIEM nguoi choi mo
 * NPC Tong Quan, nen chi can truy van CSDL luc do (giong cach RequestNPCInfoHandler dang
 * goi Database.instance.isCharRegisterLiendau() thang trong cung case -10).
 */
public final class SuKienDuaTop {

    public static final String TEN_BANG = "tob_top_thuong";

    /** idMenu rieng cho menu "chon 1 trong nhieu qua" (dung CHUNG cho ca cap 1 va cap
     * 2 - trang thai dang o cap nao nam trong ChoDangChon.conCapHienTai) - khong duoc
     * trung idMenu nao khac NPC -10 dang dung (0 = menu goc, 1/2/3 = cac menu con co
     * san, 100/101 = SuKienWorldCup.MENU_BXH/MENU_GOC). */
    public static final int MENU_CHON_THUONG = 102;

    private SuKienDuaTop() {
    }

    // ------------------------------------------------------------------ bang

    /** Goi mot lan luc khoi dong server (xem TeamServer). Tu tao bang, khong lam gi neu da co. */
    public static void taoBang() {
        Connection conn = null;
        Statement st = null;
        try {
            conn = Database.instance.getConnection();
            st = conn.createStatement();
            st.execute("CREATE TABLE IF NOT EXISTS `" + TEN_BANG + "` ("
                    + "`id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,"
                    + "`vi_tri` TINYINT NOT NULL,"
                    + "`charname` VARCHAR(50) NOT NULL,"
                    + "`item` TEXT NULL,"
                    + "`item_chon` TEXT NULL,"
                    + "`da_nhan` TINYINT NOT NULL DEFAULT 0,"
                    + "`admin_user` VARCHAR(50) NOT NULL DEFAULT '',"
                    + "`tao_luc` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,"
                    + "`nhan_luc` DATETIME NULL,"
                    + "PRIMARY KEY (`id`),"
                    + "KEY `idx_charname_da_nhan` (`charname`, `da_nhan`)"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
            damBaoCotItemChon(conn);
        } catch (final Exception e) {
            System.out.println("SU KIEN DUA TOP: loi tao bang - " + e);
        } finally {
            dong(st);
            tra(conn);
        }
    }

    /** Bang tao truoc khi co item_chon (19/09/2026) thi them cot. Chi ALTER khi that su thieu. */
    private static void damBaoCotItemChon(final Connection conn) throws Exception {
        PreparedStatement ps = null;
        ResultSet rs = null;
        Statement st = null;
        try {
            ps = conn.prepareStatement("SELECT COUNT(*) FROM information_schema.COLUMNS"
                    + " WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ? AND COLUMN_NAME = 'item_chon'");
            ps.setString(1, TEN_BANG);
            rs = ps.executeQuery();
            final boolean co = rs.next() && rs.getInt(1) > 0;
            if (!co) {
                st = conn.createStatement();
                st.execute("ALTER TABLE `" + TEN_BANG + "` ADD COLUMN `item_chon` TEXT NULL AFTER `item`");
                System.out.println("SU KIEN DUA TOP: da them cot item_chon vao " + TEN_BANG);
            }
        } finally {
            dong(rs);
            dong(ps);
            dong(st);
        }
    }

    // ------------------------------------------------------------------ hoi CSDL

    /** Nhan vat nay co dong thuong nao dang cho nhan khong - quyet dinh co hien muc menu. */
    public static boolean coThuongChoNhan(final String charname) {
        if (charname == null || charname.isEmpty()) {
            return false;
        }
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = Database.instance.getConnection();
            ps = conn.prepareStatement("SELECT 1 FROM `" + TEN_BANG
                    + "` WHERE charname = ? AND da_nhan = 0 LIMIT 1");
            ps.setString(1, charname);
            rs = ps.executeQuery();
            return rs.next();
        } catch (final Exception e) {
            System.out.println("SU KIEN DUA TOP: loi hoi coThuongChoNhan - " + e);
            return false;
        } finally {
            dong(rs);
            dong(ps);
            tra(conn);
        }
    }

    /** Mot dong thuong dang cho nhan. */
    private static final class DongThuong {
        long id;
        int viTri;
        String item;
        String itemChon;
    }

    private static List<DongThuong> layThuongChoNhan(final String charname) throws Exception {
        final List<DongThuong> ds = new ArrayList<DongThuong>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = Database.instance.getConnection();
            ps = conn.prepareStatement("SELECT id, vi_tri, item, item_chon FROM `" + TEN_BANG
                    + "` WHERE charname = ? AND da_nhan = 0 ORDER BY vi_tri");
            ps.setString(1, charname);
            rs = ps.executeQuery();
            while (rs.next()) {
                final DongThuong d = new DongThuong();
                d.id = rs.getLong("id");
                d.viTri = rs.getInt("vi_tri");
                d.item = rs.getString("item");
                d.itemChon = rs.getString("item_chon");
                ds.add(d);
            }
        } finally {
            dong(rs);
            dong(ps);
            tra(conn);
        }
        return ds;
    }

    private static void danhDauDaNhan(final long id) throws Exception {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = Database.instance.getConnection();
            // Chi danh dau khi dong CON dang o trang thai chua nhan: chan nhan trung neu
            // nguoi choi bam menu hai lan lien tiep truoc khi lan dau kip cap nhat xong.
            ps = conn.prepareStatement("UPDATE `" + TEN_BANG
                    + "` SET da_nhan = 1, nhan_luc = NOW() WHERE id = ? AND da_nhan = 0");
            ps.setLong(1, id);
            ps.executeUpdate();
        } finally {
            dong(ps);
            tra(conn);
        }
    }

    /**
     * Tach chuoi item_chon "nhan1=chuoi1|nhan2=chuoi2|..." thanh danh sach [nhan, chuoi].
     * Doan sai dinh dang (thieu "=", nhan hoac chuoi rong) bi BO QUA thay vi nem loi -
     * du lieu nay do admin nhap qua bo chon da kiem cu phap tung "chuoi" (kiemTraItem()),
     * chi rieng dinh dang bao boc "nhan=chuoi|..." la khong the kiem truoc luc luu.
     */
    private static List<String[]> tachItemChon(final String itemChon) {
        final List<String[]> ra = new ArrayList<String[]>();
        if (itemChon == null) {
            return ra;
        }
        for (final String doan : itemChon.split("\\|")) {
            final String s = doan.trim();
            if (s.isEmpty()) {
                continue;
            }
            final int vt = s.indexOf('=');
            if (vt <= 0 || vt >= s.length() - 1) {
                continue;
            }
            final String nhan = s.substring(0, vt).trim();
            final String chuoi = s.substring(vt + 1).trim();
            if (nhan.isEmpty() || chuoi.isEmpty()) {
                continue;
            }
            ra.add(new String[]{nhan, chuoi});
        }
        return ra;
    }

    /**
     * Gom cac cap [nhan, chuoi] cua tachItemChon() thanh cay 2 cap theo ten nhom, giu
     * dung THU TU xuat hien dau tien (LinkedHashMap) de menu hien ra dung thu tu admin
     * da them. Nhan dang "ten nhom::nhan con" (bo chon trang Dua top (HTBB-Web /admin/dua-top) sinh cho nut "Thêm
     * CẢ NHÓM") duoc tach doi; nhan KHONG co "::" (nut "Thêm" don le) tu no la mot nhom
     * CHI CO DUNG 1 nhan con (chinh no) - xem doChonThuong ve cach xu ly nhom 1 nhan con.
     */
    private static LinkedHashMap<String, List<String[]>> phanCapItemChon(final String itemChon) {
        final LinkedHashMap<String, List<String[]>> ra = new LinkedHashMap<String, List<String[]>>();
        for (final String[] cap : tachItemChon(itemChon)) {
            final String nhanGoc = cap[0];
            final String chuoi = cap[1];
            final int vt = nhanGoc.indexOf("::");
            final String tenNhom = vt >= 0 ? nhanGoc.substring(0, vt).trim() : nhanGoc;
            final String nhanCon = vt >= 0 ? nhanGoc.substring(vt + 2).trim() : nhanGoc;
            List<String[]> ds = ra.get(tenNhom);
            if (ds == null) {
                ds = new ArrayList<String[]>();
                ra.put(tenNhom, ds);
            }
            ds.add(new String[]{nhanCon, chuoi});
        }
        return ra;
    }

    // ------------------------------------------------------------------ nhan thuong

    /** Nguoi choi dang cho chon giua nhieu lua chon (2 cap) cho MOT dong thuong. */
    private static final class ChoDangChon {
        long idDong;
        int viTri;
        String itemCoDinh;
        LinkedHashMap<String, List<String[]>> nhom;
        List<String> tenNhomTheoThuTu;
        /** null = dang o CAP 1 (chon nhom); khac null = dang o CAP 2, day la danh sach
         * nhan con cua nhom vua chon o cap 1. */
        List<String[]> conCapHienTai;
    }

    /** Khoa theo charDBID: nguoi choi thoat ra vao lai la doi tuong Char khac, nhung
     * van la NHAN VAT do nen van tiep tuc dung phien chon con dang do. */
    private static final ConcurrentHashMap<Integer, ChoDangChon> DANG_CHON =
            new ConcurrentHashMap<Integer, ChoDangChon>();

    /**
     * Bam muc "Nhan thuong đua top". Cong NGAY moi dong CHI co item (khong co item_chon);
     * dong DAU TIEN co item_chon thi mo menu de nguoi choi tu chon (xem lop nay o dau file).
     */
    public static void nhanThuong(final Char player) {
        xuLyDotTiepTheo(player, true);
    }

    /**
     * Ham dung chung cho ca nhanThuong() (nguoi choi bam menu) va sau khi ap dung xong
     * mot lua chon (kiem tiep xem con dong item_chon nao khac khong).
     */
    private static void xuLyDotTiepTheo(final Char player, final boolean baoNeuKhongCoGi) {
        try {
            final List<DongThuong> ds = layThuongChoNhan(player.charname);
            if (ds.isEmpty()) {
                if (baoNeuKhongCoGi) {
                    player.sendMessage(MessageCreator.createServerAlertMessage(
                            "Bạn không có thưởng đua top nào đang chờ nhận.", ""));
                }
                return;
            }
            final StringBuilder logViTri = new StringBuilder();
            DongThuong choViTri = null;
            for (final DongThuong d : ds) {
                final boolean coChon = d.itemChon != null && !d.itemChon.trim().isEmpty();
                if (coChon) {
                    if (choViTri == null) {
                        choViTri = d;
                    }
                    continue; // xu ly rieng (ca phan item co dinh cua dong nay) khi chon xong
                }
                danhDauDaNhan(d.id);
                DatabaseGiftCode.applyItemString(player, d.item);
                if (logViTri.length() > 0) {
                    logViTri.append(',');
                }
                logViTri.append(d.viTri);
            }
            if (logViTri.length() > 0) {
                player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                player.sendMessage(MessageCreator.createServerAlertMessage(
                        "Chúc mừng bạn đã nhận thưởng đua top (hạng " + logViTri + ").", ""));
                Database.instance.saveOrtherLog("", player.charname,
                        "nhan thuong dua top hang " + logViTri, "duatop_thuong");
            }
            if (choViTri != null) {
                moMenuChonThuong(player, choViTri);
            }
        } catch (final Exception e) {
            e.printStackTrace();
            try {
                player.sendMessage(MessageCreator.createServerAlertMessage(
                        "Có lỗi khi nhận thưởng, vui lòng thử lại hoặc báo quản trị viên.", ""));
            } catch (final Exception ignored) {
            }
        }
    }

    /** Mo menu CAP 1 (chon nhom, vd chon loai mat na) cho mot dong co item_chon. */
    private static void moMenuChonThuong(final Char player, final DongThuong d) {
        final LinkedHashMap<String, List<String[]>> nhom = phanCapItemChon(d.itemChon);
        if (nhom.isEmpty()) {
            // Du lieu hong (admin nhap sai dinh dang) - bao loi ro rang thay vi im lang
            // bo qua vinh vien mot phan thuong (dong nay se cu the mai vi khong ai chon duoc).
            player.sendMessage(MessageCreator.createServerAlertMessage(
                    "Phần thưởng hạng " + d.viTri + " bị lỗi định dạng lựa chọn, hãy báo quản trị viên.", ""));
            return;
        }
        final ChoDangChon c = new ChoDangChon();
        c.idDong = d.id;
        c.viTri = d.viTri;
        c.itemCoDinh = d.item;
        c.nhom = nhom;
        c.tenNhomTheoThuTu = new ArrayList<String>(nhom.keySet());
        c.conCapHienTai = null;
        DANG_CHON.put(Integer.valueOf(player.charDBID), c);
        player.sendMessage(MessageCreator.createServerAlertMessage(
                "Hạng " + d.viTri + ": hãy chọn loại phần thưởng bạn muốn nhận.", ""));
        player.sendMessage(MessageCreator.createMsgMenu(
                c.tenNhomTheoThuTu.toArray(new String[0]), -10, MENU_CHON_THUONG));
    }

    /**
     * Nguoi choi bam mot dong trong menu "chon qua" (idMenu == MENU_CHON_THUONG) - co the
     * la CAP 1 (chon nhom) hoac CAP 2 (chon nhan con trong nhom da chon), tuy theo
     * ChoDangChon.conCapHienTai dang null hay khong.
     */
    public static void doChonThuong(final Char player, final int idOptionMenu) throws IOException {
        final ChoDangChon c = DANG_CHON.get(Integer.valueOf(player.charDBID));
        if (c == null) {
            player.sendMessage(MessageCreator.createServerAlertMessage(
                    "Phiên chọn thưởng đã hết hạn, hãy mở lại menu \"Nhận thưởng đua top\".", ""));
            return;
        }

        if (c.conCapHienTai == null) {
            // CAP 1: chon nhom.
            if (idOptionMenu < 0 || idOptionMenu >= c.tenNhomTheoThuTu.size()) {
                player.sendMessage(MessageCreator.createServerAlertMessage("Lựa chọn không hợp lệ.", ""));
                return;
            }
            final String tenNhom = c.tenNhomTheoThuTu.get(idOptionMenu);
            final List<String[]> con = c.nhom.get(tenNhom);
            if (con.size() == 1) {
                // Nhom chi co dung 1 lua chon (nhan "phang", khong co "::") - ap dung
                // luon, khong bat nguoi choi bam them mot menu chi co 1 dong.
                apDungLuaChon(player, c, con.get(0));
                return;
            }
            c.conCapHienTai = con;
            final String[] nhanCon = new String[con.size()];
            for (int i = 0; i < con.size(); i++) {
                nhanCon[i] = con.get(i)[0];
            }
            player.sendMessage(MessageCreator.createServerAlertMessage(
                    "Hạng " + c.viTri + " — " + tenNhom + ": chọn tiếp chỉ số bạn muốn.", ""));
            player.sendMessage(MessageCreator.createMsgMenu(nhanCon, -10, MENU_CHON_THUONG));
            return;
        }

        // CAP 2: chon nhan con trong nhom da chon o cap 1.
        if (idOptionMenu < 0 || idOptionMenu >= c.conCapHienTai.size()) {
            player.sendMessage(MessageCreator.createServerAlertMessage("Lựa chọn không hợp lệ.", ""));
            return;
        }
        apDungLuaChon(player, c, c.conCapHienTai.get(idOptionMenu));
    }

    /** Cong phan thuong da chon (item co dinh cua dong, neu co, cong CHUNG) va danh dau da nhan. */
    private static void apDungLuaChon(final Char player, final ChoDangChon c, final String[] nhanVaChuoi)
            throws IOException {
        try {
            danhDauDaNhan(c.idDong);
            if (c.itemCoDinh != null && !c.itemCoDinh.trim().isEmpty()) {
                DatabaseGiftCode.applyItemString(player, c.itemCoDinh);
            }
            DatabaseGiftCode.applyItemString(player, nhanVaChuoi[1]);
            DANG_CHON.remove(Integer.valueOf(player.charDBID));
            player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
            player.sendMessage(MessageCreator.createServerAlertMessage(
                    "Chúc mừng bạn đã chọn: " + nhanVaChuoi[0] + " (hạng " + c.viTri + ").", ""));
            Database.instance.saveOrtherLog("", player.charname,
                    "chon thuong dua top hang " + c.viTri + ": " + nhanVaChuoi[0], "duatop_thuong");
        } catch (final Exception e) {
            e.printStackTrace();
            player.sendMessage(MessageCreator.createServerAlertMessage(
                    "Có lỗi khi nhận thưởng, vui lòng thử lại hoặc báo quản trị viên.", ""));
            return;
        }
        // Con dong item_chon nao khac dang cho khong (nhieu hang cung co qua tu chon).
        xuLyDotTiepTheo(player, false);
    }

    // ------------------------------------------------------------------ tien ich

    private static void dong(final ResultSet rs) {
        try {
            if (rs != null) {
                rs.close();
            }
        } catch (final Exception ignored) {
        }
    }

    private static void dong(final Statement st) {
        try {
            if (st != null) {
                st.close();
            }
        } catch (final Exception ignored) {
        }
    }

    private static void tra(final Connection conn) {
        try {
            if (conn != null) {
                Database.connPool.free(conn);
            }
        } catch (final Exception ignored) {
        }
    }
}
