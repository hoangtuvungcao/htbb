package server;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.Properties;

/**
 * DOC / GHI 26 SETTING NAP LAI DUOC LUC DANG CHAY - bang `htbb_db.cau_hinh`.
 *
 * TRUOC 09/09/2026: nguon du lieu la file server.ini, sua qua bang setting trong game
 * (real/AdminMenu.java) hoac sua tay file roi go lenh "nap lai". File rieng tren tung
 * may (khong nam trong git), sua tu xa phai SSH.
 *
 * NAY: nguon du lieu la bang SQL `cau_hinh` (khoa, gia_tri, sua_boi, sua_luc). Web
 * (HTBB-Web /admin/cauhinh) va bang setting trong game GHI CHUNG mot bang. Server tu
 * doc lai bang nay moi 10 giay (xem batDau()) nen admin sua tren web thi may chu ap
 * dung trong toi da 10 giay, khong can restart, khong can go lenh "nap lai" trong game
 * nua (van con dung duoc, chi la khong bat buoc).
 *
 * server.ini VAN GIU 26 khoa nay (xem vps/server.ini.tmpl) lam GIA TRI KHOI TAO LAN DAU
 * - dungBoLucKhoiDong() chi INSERT IGNORE tu file vao SQL khi bang con thieu dong, roi
 * KHONG DUNG TOI FILE NUA. Giu nguyen ten hang so K_* va gia tri chuoi cua chung de hai
 * bai kiem tra test/KiemCauHinhPipeline.java va test/KiemTaiLieuCauHinh.java (doi chieu
 * server.ini.tmpl <-> ma nguon <-> docs/cau-hinh.md) khong bi vo.
 */
public final class CauHinh {

    private static final String BANG = "cau_hinh";

    /** Cac khoa dung cho bang setting - TEN GIU NGUYEN, chi doi noi luu tru. */
    public static final String K_GIO_CT = "sv.gioChiemThanh";
    public static final String K_PHUT_CT = "sv.phutChiemThanh";
    public static final String K_NGAY_CT = "sv.ngayChiemThanh";
    public static final String K_TILE_XU = "sv.tileXu";
    public static final String K_TILE_LUONG = "sv.tileLuong";
    public static final String K_TILE_SUKIEN = "sv.tileItemSuKien";
    public static final String K_TILE_POTION = "sv.tilePotion";
    public static final String K_TILE_NGOC = "sv.tileNgoc";
    public static final String K_TILE_ITEM = "sv.tileItem";
    public static final String K_CHIA_XU = "sv.chiaXuRoi";
    /** % so voi ti le goc cua EXP quai thuong (Monster, khong dung Boss). Xem Monster.rcvXpSauGiam(). */
    public static final String K_TILE_EXP_LV50 = "sv.tileExpLv50";
    public static final String K_TILE_EXP_DUOI_LV50 = "sv.tileExpDuoiLv50";
    /**
     * Ti le mau + he so EXP khi nhan/tra Van Tieu. Xem Char.TIEU_NGUONG_MAU (CSV 5 gia tri
     * cong don, tren thang 100000) / TIEU_HE_SO_EXP (% nhan, 200 = x2) / TIEU_PC_EXP_THEO_MAU
     * (CSV 7 gia tri) / TIEU_PERMILLE_CAO_CAP (CSV 9 gia tri) va CharSuKien.doResponseTieu().
     */
    public static final String K_TIEU_NGUONG_MAU = "sv.tieuNguongMau";
    public static final String K_TIEU_HE_SO_EXP = "sv.tieuHeSoExp";
    public static final String K_TIEU_PC_EXP_THEO_MAU = "sv.tieuPcExpTheoMau";
    public static final String K_TIEU_PERMILLE_CAO_CAP = "sv.tieuPermilleCaoCap";
    /**
     * Ti le mau khi DOI tieu (reroll tra phi), doc lap voi K_TIEU_NGUONG_MAU (luc nhan
     * tieu moi). Xem Char.DOI_TIEU_NGUONG_MAU_XU / _LUONG, Char.doChangeTieu().
     */
    public static final String K_DOI_TIEU_NGUONG_MAU_XU = "sv.doiTieuNguongMauXu";
    public static final String K_DOI_TIEU_NGUONG_MAU_LUONG = "sv.doiTieuNguongMauLuong";
    public static final String K_TAO_TEN_GM = "sv.choPhepTaoTenGM";
    /** Cua so thoi gian su kien World Cup, dinh dang "yyyy-MM-dd HH:mm:ss". */
    public static final String K_WC_BAT_DAU = "sv.worldCupBatDau";
    public static final String K_WC_KET_THUC = "sv.worldCupKetThuc";
    public static final String K_WC_HAN_NHAN_QUA = "sv.worldCupHanNhanQua";
    /** Lich hen mo/dong su kien + tu khoi dong lai. Xem real/LichSuKien. */
    public static final String K_SK_TU_KHOI_DONG_LAI = "sv.suKienTuKhoiDongLai";
    public static final String K_SK_GOM_GIAY = "sv.suKienGomGiay";
    public static final String K_SK_BAO_TRUOC_GIAY = "sv.suKienBaoTruocGiay";
    /**
     * 3 co thuong EXP phu - truoc 11/09/2026 doc bang Byte.parseByte truc tiep tu server.ini
     * trong TeamServer (bat buoc phai co dong, thieu la nem loi ngay luc khoi dong). Chuyen
     * vao day de sua duoc qua trang HTBB-Web /admin/cauhinh, khong can restart. Xem noi dung tung
     * co: CharSuKien.java (ExpVantieu - EXP giao tieu ca nhan), InfoGifLucky.java (LuckyExp -
     * o thuong EXP trong Ruong May Man), FruitTemplate.java (ExpQua - EXP cay an qua; luu y
     * canTangToc() cung dung co nay de khoa nut "tang toc" cho MOI loai cay, khong rieng cay
     * EXP - hieu ung phu, khong phai loi cua lan chuyen doi nay).
     */
    public static final String K_EXP_VANTIEU = "sv.expvantieu";
    public static final String K_LUCKY_EXP = "sv.Luckyexp";
    public static final String K_EXP_QUA = "sv.ExpQua";

    /** Tat ca 34 khoa, dung de dong bo luc khoi dong va de web biet liet ke gi. */
    private static final String[] TAT_CA_KHOA = {
        K_TILE_XU, K_TILE_LUONG, K_TILE_SUKIEN, K_TILE_POTION, K_TILE_NGOC, K_TILE_ITEM,
        K_CHIA_XU, K_GIO_CT, K_PHUT_CT, K_NGAY_CT,
        K_WC_BAT_DAU, K_WC_KET_THUC, K_WC_HAN_NHAN_QUA,
        K_SK_TU_KHOI_DONG_LAI, K_SK_GOM_GIAY, K_SK_BAO_TRUOC_GIAY,
        K_TAO_TEN_GM, "sv.maxCharPerAccount", "sv.adminAccounts",
        "sv.website", "sv.linkTaiGame", "sv.testMode", "sv.testCode",
        K_EXP_VANTIEU, K_LUCKY_EXP, K_EXP_QUA,
        K_TILE_EXP_LV50, K_TILE_EXP_DUOI_LV50,
        K_TIEU_NGUONG_MAU, K_TIEU_HE_SO_EXP, K_TIEU_PC_EXP_THEO_MAU, K_TIEU_PERMILLE_CAO_CAP,
        K_DOI_TIEU_NGUONG_MAU_XU, K_DOI_TIEU_NGUONG_MAU_LUONG,
    };

    /** Giay giua hai lan tu doc lai bang cau_hinh. Xem batDau(). */
    private static final long CHU_KY_NAP_LAI_MS = 10000L;

    private static volatile boolean daBatDau = false;

    private CauHinh() {
    }

    // =========================== BANG SQL ===========================

    private static void taoBangNeuThieu(final Connection conn) throws Exception {
        final Statement stmt = conn.createStatement();
        try {
            stmt.execute(
                    "CREATE TABLE IF NOT EXISTS `" + BANG + "` ("
                    + "`khoa` varchar(64) NOT NULL,"
                    + "`gia_tri` varchar(255) NOT NULL DEFAULT '',"
                    + "`sua_boi` varchar(50) DEFAULT NULL,"
                    + "`sua_luc` datetime DEFAULT NULL,"
                    + "PRIMARY KEY (`khoa`)"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
        } finally {
            stmt.close();
        }
    }

    /** Doc toan bo bang cau_hinh thanh Properties (de tai dung nguyen cac ham apDung*). */
    private static Properties docTuSql(final Connection conn) throws Exception {
        final Properties p = new Properties();
        final Statement stmt = conn.createStatement();
        try {
            final ResultSet rs = stmt.executeQuery("SELECT khoa, gia_tri FROM `" + BANG + "`");
            try {
                while (rs.next()) {
                    p.setProperty(rs.getString("khoa"), rs.getString("gia_tri"));
                }
            } finally {
                rs.close();
            }
        } finally {
            stmt.close();
        }
        return p;
    }

    // ============================ GHI ============================

    public static boolean luu(final String key, final String value) {
        final LinkedHashMap<String, String> m = new LinkedHashMap<String, String>();
        m.put(key, value);
        return luuNhieu(m);
    }

    public static boolean luu(final String key, final int value) {
        return luu(key, String.valueOf(value));
    }

    /** Ghi nhieu khoa trong mot lan mo ket noi. "game" lam sua_boi (bang setting trong game). */
    public static synchronized boolean luuNhieu(final LinkedHashMap<String, String> capGiaTri) {
        return luuNhieu(capGiaTri, "game");
    }

    /** Ghi nhieu khoa, ghi ro ai sua (web goi voi ten dang nhap admin). */
    public static synchronized boolean luuNhieu(final LinkedHashMap<String, String> capGiaTri, final String nguoiSua) {
        if (capGiaTri == null || capGiaTri.isEmpty()) {
            return true;
        }
        Connection conn = null;
        PreparedStatement pre = null;
        try {
            conn = data.Database.instance.getConnection();
            taoBangNeuThieu(conn);
            pre = conn.prepareStatement(
                    "INSERT INTO `" + BANG + "` (khoa, gia_tri, sua_boi, sua_luc) VALUES (?, ?, ?, NOW())"
                    + " ON DUPLICATE KEY UPDATE gia_tri = VALUES(gia_tri), sua_boi = VALUES(sua_boi), sua_luc = VALUES(sua_luc)");
            for (final java.util.Map.Entry<String, String> e : capGiaTri.entrySet()) {
                pre.setString(1, e.getKey());
                pre.setString(2, e.getValue());
                pre.setString(3, nguoiSua);
                pre.executeUpdate();
            }
            // Cau hinh trung tam (com.htbb.cauhinh): bao co thay doi -> nap lai + ghi lich su trong <= 5 giay.
            com.htbb.cauhinh.CauHinhTrungTam.tangPhienBan(nguoiSua);
            return true;
        } catch (final Exception e) {
            System.out.println("[CAUHINH] loi ghi " + BANG + ": " + e);
            return false;
        } finally {
            try {
                if (pre != null) {
                    pre.close();
                }
            } catch (final Exception ignored) {
            }
            try {
                data.Database.connPool.free(conn);
            } catch (final Exception ignored) {
            }
        }
    }

    // ==================== DONG BO LUC KHOI DONG ====================

    /**
     * Goi 1 lan luc khoi dong server, SAU khi da ket noi duoc CSDL. Tao bang cau_hinh
     * neu chua co, chep (INSERT IGNORE - khong ghi de dong da co) 23 gia tri dang doc
     * duoc tu server.ini (Properties da nap o dau ham chinh cua TeamServer) vao SQL, roi
     * ap dung ngay tu SQL. Tu lan khoi dong thu hai tro di bang da co du dong nen buoc
     * chep khong lam gi ca - SQL la nguon that.
     */
    public static synchronized void dongBoLucKhoiDong(final Properties pFile) {
        Connection conn = null;
        PreparedStatement pre = null;
        try {
            conn = data.Database.instance.getConnection();
            taoBangNeuThieu(conn);
            pre = conn.prepareStatement(
                    "INSERT IGNORE INTO `" + BANG + "` (khoa, gia_tri, sua_boi, sua_luc) VALUES (?, ?, 'khoi-dong-lan-dau', NOW())");
            for (final String k : TAT_CA_KHOA) {
                final String v = pFile.getProperty(k);
                if (v != null) {
                    pre.setString(1, k);
                    pre.setString(2, v.trim());
                    pre.executeUpdate();
                }
            }
        } catch (final Exception e) {
            System.out.println("[CAUHINH] loi dong bo lan dau: " + e);
        } finally {
            try {
                if (pre != null) {
                    pre.close();
                }
            } catch (final Exception ignored) {
            }
            try {
                data.Database.connPool.free(conn);
            } catch (final Exception ignored) {
            }
        }
        System.out.println(napLai());
    }

    /**
     * Vong lap nen: tu doc lai bang cau_hinh moi CHU_KY_NAP_LAI_MS, de thay doi tu trang
     * quan tri web ap dung ma khong can restart. Goi 1 lan luc khoi dong (sau
     * dongBoLucKhoiDong()); goi lai lan nua khong lam gi (chan boi daBatDau).
     */
    public static synchronized void batDau() {
        if (daBatDau) {
            return;
        }
        daBatDau = true;
        // Tu 18/10/2026: vong nap lai thuoc com.htbb.cauhinh.CauHinhTrungTam (hoi phien ban moi 5 giay, doc ca
        // bang khi doi / moi 60 giay, kiem gia tri, bao nguoi nghe, ghi xac nhan). Ham apDung() ben duoi la mot
        // nguoi nghe cua no. CHU_KY_NAP_LAI_MS giu lai lam tran tren (<= 10 giay) cho tai lieu.
        com.htbb.cauhinh.CauHinhTrungTam.batDau();
    }

    // =========================== NAP LAI ===========================

    /**
     * Nap lai tu bang cau_hinh nhung THAM SO CO THE DOI LUC DANG CHAY.
     *
     * CO Y KHONG nap lai: cong, ket noi DB, duong dan log, gioi han CCU... doi cac thu
     * do luc server dang chay se lam dut ket noi hoac tao pool DB thu hai. Muon doi
     * chung thi restart (van nam trong server.ini).
     *
     * @return mo ta ket qua de hien cho admin
     */
    public static String napLai() {
        // Doc + kiem + ap dung qua cau hinh trung tam (CSDL > server.ini > mac dinh); no goi apDung() ben duoi.
        return com.htbb.cauhinh.CauHinhTrungTam.napLaiNgay("lenh-nap-lai");
    }

    /**
     * Ap Properties da kiem (cau hinh trung tam: server.ini + bang cau_hinh, gia tri sai da bi tu choi) vao
     * bien tinh cua server. Goi tu com.htbb.cauhinh.ApDungMacDinh moi khi cau hinh doi. Khong dong bo
     * (synchronized) co y: chi chay tren luong nap lai cua CauHinhTrungTam (da khoa san), tranh khoa cheo voi
     * luuNhieu().
     */
    public static void apDung(final Properties p) {
        real.Monster.PC_TILE_XU = kep(p.getProperty(K_TILE_XU), real.Monster.PC_TILE_XU);
        real.Monster.PC_TILE_LUONG = kep(p.getProperty(K_TILE_LUONG), real.Monster.PC_TILE_LUONG);
        real.Monster.PC_TILE_ITEM_SUKIEN = kep(p.getProperty(K_TILE_SUKIEN), real.Monster.PC_TILE_ITEM_SUKIEN);
        real.Monster.PC_TILE_POTION = kep(p.getProperty(K_TILE_POTION), real.Monster.PC_TILE_POTION);
        real.Monster.PC_TILE_NGOC = kep(p.getProperty(K_TILE_NGOC), real.Monster.PC_TILE_NGOC);
        real.Monster.PC_TILE_ITEM = kep(p.getProperty(K_TILE_ITEM), real.Monster.PC_TILE_ITEM);
        real.Map.DIV_DROP_XU = Math.max(1, so(p.getProperty(K_CHIA_XU), real.Map.DIV_DROP_XU));
        TeamServer.tileGoc = new int[]{real.Monster.PC_TILE_XU, real.Monster.PC_TILE_LUONG,
            real.Monster.PC_TILE_ITEM_SUKIEN, real.Monster.PC_TILE_POTION,
            real.Monster.PC_TILE_NGOC, real.Monster.PC_TILE_ITEM, real.Map.DIV_DROP_XU};

        real.Monster.PC_TILE_EXP_LV50 = kep(p.getProperty(K_TILE_EXP_LV50), real.Monster.PC_TILE_EXP_LV50);
        real.Monster.PC_TILE_EXP_DUOI_LV50 = kep(p.getProperty(K_TILE_EXP_DUOI_LV50), real.Monster.PC_TILE_EXP_DUOI_LV50);

        real.Char.TIEU_NGUONG_MAU = mang(p.getProperty(K_TIEU_NGUONG_MAU), real.Char.TIEU_NGUONG_MAU);
        real.Char.TIEU_HE_SO_EXP = Math.max(0, so(p.getProperty(K_TIEU_HE_SO_EXP), real.Char.TIEU_HE_SO_EXP));
        real.Char.TIEU_PC_EXP_THEO_MAU = mang(p.getProperty(K_TIEU_PC_EXP_THEO_MAU), real.Char.TIEU_PC_EXP_THEO_MAU);
        real.Char.TIEU_PERMILLE_CAO_CAP = mang(p.getProperty(K_TIEU_PERMILLE_CAO_CAP), real.Char.TIEU_PERMILLE_CAO_CAP);

        real.Char.DOI_TIEU_NGUONG_MAU_XU = mang(p.getProperty(K_DOI_TIEU_NGUONG_MAU_XU), real.Char.DOI_TIEU_NGUONG_MAU_XU);
        real.Char.DOI_TIEU_NGUONG_MAU_LUONG = mang(p.getProperty(K_DOI_TIEU_NGUONG_MAU_LUONG), real.Char.DOI_TIEU_NGUONG_MAU_LUONG);

        apDungGioChiemThanh(p);
        // Thiet ke dai han: doc de so lieu (khoa cua data_kinh_te_lau_dai, vd thue.cho).
        com.htbb.pokemon.LauDai.apDungCauHinh(p);
        // thanh vien: thanh_vien.* (bat, chuc_nang, mien_cap, thong_bao...) doc lai moi 10 giay.
        com.htbb.pokemon.ThanhVien.apDungCauHinh(p);
        data.Database.CHO_PHEP_TAO_TEN_GM = "1".equals(nhan(p.getProperty(K_TAO_TEN_GM), "0"));

        int max = so(p.getProperty("sv.maxCharPerAccount"), data.Database.MAX_CHAR_PER_ACCOUNT);
        if (max < 1) {
            max = 1;
        }
        if (max > 3) {
            max = 3;
        }
        data.Database.MAX_CHAR_PER_ACCOUNT = max;

        TeamServer.adminAccounts.clear();
        final String ds = nhan(p.getProperty("sv.adminAccounts"), "");
        if (!ds.isEmpty()) {
            for (final String a : ds.split(",")) {
                final String an = a.trim().toLowerCase();
                if (!an.isEmpty()) {
                    TeamServer.adminAccounts.add(an);
                }
            }
        }
        TeamServer.website = nhan(p.getProperty("sv.website"), "");
        TeamServer.linkTaiGame = nhan(p.getProperty("sv.linkTaiGame"), "");
        TeamServer.testMode = "1".equals(nhan(p.getProperty("sv.testMode"), "0"));
        TeamServer.testCode = nhan(p.getProperty("sv.testCode"), "").toLowerCase();

        apDungSuKienWorldCup(p);
        apDungLichSuKien(p);

        TeamServer.ExpVantieu = "1".equals(nhan(p.getProperty(K_EXP_VANTIEU), "0"));
        TeamServer.LuckyExp = "1".equals(nhan(p.getProperty(K_LUCKY_EXP), "0"));
        TeamServer.ExpQua = "1".equals(nhan(p.getProperty(K_EXP_QUA), "0"));
    }

    /**
     * Ap dung cua so thoi gian su kien World Cup. Dung ca luc khoi dong.
     *
     * De trong bat ky khoa nao = su kien TAT (quai khong roi bong vang, menu khong hien).
     * Xem real/SuKienWorldCup.
     */
    public static void apDungSuKienWorldCup(final Properties p) {
        real.SuKienWorldCup.datThoiGian(
                nhan(p.getProperty(K_WC_BAT_DAU), ""),
                nhan(p.getProperty(K_WC_KET_THUC), ""),
                nhan(p.getProperty(K_WC_HAN_NHAN_QUA), ""));
    }

    /**
     * Ap dung cau hinh lich hen su kien. Dung ca luc khoi dong.
     *
     * Hai moc thoi gian bi KEP TRONG KHOANG chu khong nhan nguyen gia tri nguoi go:
     *   - gom  : 30..3600 giay. Duoi 30 thi bat vai su kien lien tay se thanh nhieu lan
     *            khoi dong lai va dung phai StartLimitBurst=5/300s cua systemd -> unit
     *            "failed", server nam han. Xem chot an toan trong real/LichSuKien.
     *   - bao truoc: 10..600 giay. Duoi 10 giay thi tin bao khong kip troi het hang doi
     *            cua client (moi tin mat 15-20 giay) - nguoi choi bi da ma khong thay
     *            chu nao, con tin thi hien lai sau khi dang nhap lai. Xem BaoTriDaily.
     */
    public static void apDungLichSuKien(final Properties p) {
        real.LichSuKien.tuKhoiDongLai =
                !"0".equals(nhan(p.getProperty(K_SK_TU_KHOI_DONG_LAI), "1"));
        real.LichSuKien.gomGiay =
                trongKhoang(so(p.getProperty(K_SK_GOM_GIAY), 120), 30, 3600);
        real.LichSuKien.baoTruocGiay =
                trongKhoang(so(p.getProperty(K_SK_BAO_TRUOC_GIAY), 60), 10, 600);
    }

    private static int trongKhoang(final int v, final int nhoNhat, final int lonNhat) {
        if (v < nhoNhat) {
            return nhoNhat;
        }
        return v > lonNhat ? lonNhat : v;
    }

    /** Ap dung gio chiem thanh trong file vao MapTown. Dung ca luc khoi dong. */
    public static void apDungGioChiemThanh(final Properties p) {
        real.MapTown.TimeAdminCheat = so(p.getProperty(K_GIO_CT), -1);
        real.MapTown.minuteAdminCheat = so(p.getProperty(K_PHUT_CT), -1);
        real.MapTown.dayAdminCheat = nhan(p.getProperty(K_NGAY_CT), "");
        if (real.MapTown.TimeAdminCheat < 0 || real.MapTown.TimeAdminCheat > 23) {
            real.MapTown.TimeAdminCheat = -1;
            real.MapTown.minuteAdminCheat = -1;
        }
        if (real.MapTown.minuteAdminCheat < 0 || real.MapTown.minuteAdminCheat > 59) {
            real.MapTown.minuteAdminCheat = 0;
        }
    }

    /** Ghi gio chiem thanh dang dat trong bo nho xuong SQL. */
    public static boolean luuGioChiemThanh() {
        final LinkedHashMap<String, String> m = new LinkedHashMap<String, String>();
        m.put(K_GIO_CT, String.valueOf(real.MapTown.TimeAdminCheat));
        m.put(K_PHUT_CT, String.valueOf(real.MapTown.minuteAdminCheat));
        m.put(K_NGAY_CT, real.MapTown.dayAdminCheat == null ? "" : real.MapTown.dayAdminCheat);
        return luuNhieu(m);
    }

    /** Ghi ti le drop dang dat trong bo nho xuong SQL. */
    public static boolean luuTiLeDrop() {
        final LinkedHashMap<String, String> m = new LinkedHashMap<String, String>();
        m.put(K_TILE_XU, String.valueOf(real.Monster.PC_TILE_XU));
        m.put(K_TILE_LUONG, String.valueOf(real.Monster.PC_TILE_LUONG));
        m.put(K_TILE_SUKIEN, String.valueOf(real.Monster.PC_TILE_ITEM_SUKIEN));
        m.put(K_TILE_POTION, String.valueOf(real.Monster.PC_TILE_POTION));
        m.put(K_TILE_NGOC, String.valueOf(real.Monster.PC_TILE_NGOC));
        m.put(K_TILE_ITEM, String.valueOf(real.Monster.PC_TILE_ITEM));
        m.put(K_CHIA_XU, String.valueOf(real.Map.DIV_DROP_XU));
        return luuNhieu(m);
    }

    /** Ghi ti le EXP quai (theo level) dang dat trong bo nho xuong SQL. */
    public static boolean luuTiLeExpQuai() {
        final LinkedHashMap<String, String> m = new LinkedHashMap<String, String>();
        m.put(K_TILE_EXP_LV50, String.valueOf(real.Monster.PC_TILE_EXP_LV50));
        m.put(K_TILE_EXP_DUOI_LV50, String.valueOf(real.Monster.PC_TILE_EXP_DUOI_LV50));
        return luuNhieu(m);
    }

    /** Ghi ti le mau + he so EXP Van Tieu dang dat trong bo nho xuong SQL. */
    public static boolean luuTiLeTieu() {
        final LinkedHashMap<String, String> m = new LinkedHashMap<String, String>();
        m.put(K_TIEU_NGUONG_MAU, joinCSV(real.Char.TIEU_NGUONG_MAU));
        m.put(K_TIEU_HE_SO_EXP, String.valueOf(real.Char.TIEU_HE_SO_EXP));
        m.put(K_TIEU_PC_EXP_THEO_MAU, joinCSV(real.Char.TIEU_PC_EXP_THEO_MAU));
        m.put(K_TIEU_PERMILLE_CAO_CAP, joinCSV(real.Char.TIEU_PERMILLE_CAO_CAP));
        return luuNhieu(m);
    }

    private static String joinCSV(final int[] a) {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < a.length; i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(a[i]);
        }
        return sb.toString();
    }

    // =========================== TIEN ICH ===========================

    private static String nhan(final String v, final String macDinh) {
        return (v == null) ? macDinh : v.trim();
    }

    private static int so(final String v, final int macDinh) {
        try {
            return Integer.parseInt(v.trim());
        } catch (final Exception e) {
            return macDinh;
        }
    }

    private static int kep(final String v, final int macDinh) {
        int n = so(v, macDinh);
        if (n < 0) {
            n = 0;
        }
        if (n > 100) {
            n = 100;
        }
        return n;
    }

    /**
     * Doc chuoi CSV "a,b,c" thanh int[]. Sai dinh dang, rong, hoac SAI DO DAI (khac
     * macDinh.length) thi giu nguyen macDinh - tranh mot mang ngan/dai lam lech chi
     * so o noi doc (vd Char.TIEU_NGUONG_MAU can dung 5 phan tu, TIEU_PERMILLE_CAO_CAP
     * can dung 9 phan tu).
     */
    private static int[] mang(final String v, final int[] macDinh) {
        if (v == null || v.trim().isEmpty()) {
            return macDinh;
        }
        final String[] phan = v.trim().split(",");
        if (phan.length != macDinh.length) {
            return macDinh;
        }
        final int[] ra = new int[phan.length];
        try {
            for (int i = 0; i < phan.length; i++) {
                ra[i] = Integer.parseInt(phan[i].trim());
            }
        } catch (final Exception e) {
            return macDinh;
        }
        return ra;
    }
}
