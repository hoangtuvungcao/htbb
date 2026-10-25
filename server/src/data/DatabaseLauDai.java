package data;

import com.htbb.pokemon.ChiSoKinhTe;
import com.htbb.pokemon.LauDai;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * CSDL cua thiet ke dai han (giai doan 2). Bang: tools/luu_tru_sinh_du_lieu/sql_nguon/them_bang_lau_dai.sql (cung tu tao o
 * {@link #taoBang} luc khoi dong khi sv.lau_dai=1). MOI cau SQL dung PreparedStatement
 * (CODING_CONVENTION muc 13). Loi CSDL khong nem ra ngoai: in log va tra gia tri an toan.
 */
public final class DatabaseLauDai {

    private DatabaseLauDai() {
    }

    private static final String[] TAO_BANG = {
        "CREATE TABLE IF NOT EXISTS tob_kinh_te_ngay (ngay DATE NOT NULL, so_nguoi INT NOT NULL DEFAULT 0,"
            + " cap_trung_vi INT NOT NULL DEFAULT 0, chi_so_gia BIGINT NOT NULL DEFAULT 0, tong_xu BIGINT NOT NULL DEFAULT 0,"
            + " tong_luong BIGINT NOT NULL DEFAULT 0, tong_luong_khoa BIGINT NOT NULL DEFAULT 0,"
            + " cung_tien_thuc_x100 BIGINT NOT NULL DEFAULT 0, top1_phan_tram_x100 INT NOT NULL DEFAULT 0,"
            + " xu_vao BIGINT NOT NULL DEFAULT 0, xu_ra BIGINT NOT NULL DEFAULT 0, muc TINYINT NOT NULL DEFAULT 0,"
            + " ghi_luc DATETIME NULL, PRIMARY KEY (ngay)) DEFAULT CHARSET=utf8",
        "CREATE TABLE IF NOT EXISTS tob_char_lau_dai (id_char INT NOT NULL, du_lieu VARCHAR(2000) NOT NULL DEFAULT '',"
            + " cap_nhat DATETIME NULL, PRIMARY KEY (id_char)) DEFAULT CHARSET=utf8",
        "CREATE TABLE IF NOT EXISTS tob_nhiem_vu_lau_dai (id_char INT NOT NULL, ma VARCHAR(32) NOT NULL,"
            + " tien_do INT NOT NULL DEFAULT 0, trang_thai TINYINT NOT NULL DEFAULT 0, tuan INT NOT NULL DEFAULT 0,"
            + " PRIMARY KEY (id_char, ma)) DEFAULT CHARSET=utf8",
        "CREATE TABLE IF NOT EXISTS tob_mua_tuan (id_char INT NOT NULL, id_shop INT NOT NULL, tuan INT NOT NULL,"
            + " so_luong INT NOT NULL DEFAULT 0, PRIMARY KEY (id_char, id_shop, tuan)) DEFAULT CHARSET=utf8",
        "CREATE TABLE IF NOT EXISTS tob_boss_dong_gop (lan BIGINT NOT NULL, id_char INT NOT NULL,"
            + " phan_nghin INT NOT NULL DEFAULT 0, hang INT NOT NULL DEFAULT 0, bac_thuong TINYINT NOT NULL DEFAULT 0,"
            + " ghi_luc DATETIME NULL, PRIMARY KEY (lan, id_char)) DEFAULT CHARSET=utf8",
        "CREATE TABLE IF NOT EXISTS tob_ham_nguc (ngay INT NOT NULL, id_char INT NOT NULL, tang INT NOT NULL DEFAULT 0,"
            + " ghi_luc DATETIME NULL, PRIMARY KEY (ngay, id_char)) DEFAULT CHARSET=utf8",
        "CREATE TABLE IF NOT EXISTS tob_pvp_mua (mua INT NOT NULL, id_char INT NOT NULL, elo INT NOT NULL DEFAULT 1000,"
            + " thang INT NOT NULL DEFAULT 0, thua INT NOT NULL DEFAULT 0, PRIMARY KEY (mua, id_char)) DEFAULT CHARSET=utf8",
        "CREATE TABLE IF NOT EXISTS tob_su_pham (id_tro INT NOT NULL, id_thay INT NOT NULL, bat_dau DATETIME NULL,"
            + " moc VARCHAR(64) NOT NULL DEFAULT '', PRIMARY KEY (id_tro)) DEFAULT CHARSET=utf8",
        "CREATE TABLE IF NOT EXISTS tob_char_xp_cu (id INT NOT NULL, xp BIGINT NOT NULL DEFAULT 0,"
            + " lastLv INT NOT NULL DEFAULT 0, xp_moi BIGINT NOT NULL DEFAULT 0, PRIMARY KEY (id)) DEFAULT CHARSET=utf8",
        "CREATE TABLE IF NOT EXISTS tob_giftcode (ma VARCHAR(32) NOT NULL, phan_thuong VARCHAR(500) NOT NULL,"
            + " han DATETIME NULL, tong_luot INT NOT NULL DEFAULT 1, da_dung INT NOT NULL DEFAULT 0,"
            + " cap_toi_thieu INT NOT NULL DEFAULT 1, bat TINYINT NOT NULL DEFAULT 1, ghi_chu VARCHAR(255) NOT NULL DEFAULT '',"
            + " tao_boi VARCHAR(50) NULL, tao_luc DATETIME NULL, PRIMARY KEY (ma)) DEFAULT CHARSET=utf8",
        "CREATE TABLE IF NOT EXISTS tob_giftcode_log (ma VARCHAR(32) NOT NULL, user_id INT NOT NULL, id_char INT NOT NULL,"
            + " charname VARCHAR(30) NOT NULL DEFAULT '', phan_thuong VARCHAR(500) NOT NULL DEFAULT '', gia_tri INT NOT NULL DEFAULT 0,"
            + " thang INT NOT NULL DEFAULT 0, luc DATETIME NULL, PRIMARY KEY (ma, user_id)) DEFAULT CHARSET=utf8",
        "CREATE TABLE IF NOT EXISTS tob_qua_online (user_id INT NOT NULL, ngay INT NOT NULL DEFAULT 0,"
            + " giay INT NOT NULL DEFAULT 0, moc INT NOT NULL DEFAULT 0, chuoi_buoc INT NOT NULL DEFAULT 0,"
            + " chuoi_ngay INT NOT NULL DEFAULT 0, thang INT NOT NULL DEFAULT 0, ngay_thang INT NOT NULL DEFAULT 0,"
            + " thang_da_thuong INT NOT NULL DEFAULT 0, PRIMARY KEY (user_id)) DEFAULT CHARSET=utf8",
        "CREATE TABLE IF NOT EXISTS tob_chiem_thanh_tuan (lan BIGINT NOT NULL, id_char INT NOT NULL,"
            + " id_clan INT NOT NULL DEFAULT -1, diem BIGINT NOT NULL DEFAULT 0, thuong VARCHAR(255) NOT NULL DEFAULT '',"
            + " luc DATETIME NULL, PRIMARY KEY (lan, id_char)) DEFAULT CHARSET=utf8",
        "CREATE TABLE IF NOT EXISTS tob_pokedex_shiny (id_char INT NOT NULL, dex SMALLINT NOT NULL,"
            + " so_shiny INT NOT NULL DEFAULT 0, PRIMARY KEY (id_char, dex)) DEFAULT CHARSET=utf8",
    };

    public static void taoBang() {
        Connection conn = null;
        PreparedStatement pre = null;
        try {
            conn = Database.instance.getConnection();
            for (final String sql : TAO_BANG) {
                pre = conn.prepareStatement(sql);
                pre.executeUpdate();
                pre.close();
                pre = null;
            }
            System.out.println("[LAU DAI] " + TAO_BANG.length + " bang san sang.");
        } catch (final Exception e) {
            System.out.println("[LAU DAI] LOI tao bang: " + e);
        } finally {
            dong(null, pre, conn);
        }
    }

    // ------------------------------------------------------------------ bat/tat tu dong (18/10)

    /** Bang can CA KHI TAT CO (chuyen EXP nguoc lai): nhat ky chuyen + cau_hinh + bang moc cu. */
    public static void taoBangChuyenDoi() {
        ghi("CREATE TABLE IF NOT EXISTS tob_char_xp_chuyen (lan INT NOT NULL, id INT NOT NULL, huong VARCHAR(8) NOT NULL,"
                + " xp_truoc BIGINT NOT NULL DEFAULT 0, xp_sau BIGINT NOT NULL DEFAULT 0, lv INT NOT NULL DEFAULT 0,"
                + " luc DATETIME NULL, PRIMARY KEY (lan, id)) DEFAULT CHARSET=utf8");
        ghi("CREATE TABLE IF NOT EXISTS tob_char_xp_cu (id INT NOT NULL, xp BIGINT NOT NULL DEFAULT 0,"
                + " lastLv INT NOT NULL DEFAULT 0, xp_moi BIGINT NOT NULL DEFAULT 0, PRIMARY KEY (id)) DEFAULT CHARSET=utf8");
        ghi("CREATE TABLE IF NOT EXISTS cau_hinh (khoa VARCHAR(64) NOT NULL, gia_tri VARCHAR(255) NOT NULL DEFAULT '',"
                + " sua_boi VARCHAR(50) NULL, sua_luc DATETIME NULL, PRIMARY KEY (khoa)) DEFAULT CHARSET=utf8mb4");
    }

    /** Doc mot khoa cau_hinh; null neu khong co / loi. */
    public static String docKhoa(final String khoa) {
        Connection conn = null;
        PreparedStatement pre = null;
        ResultSet rs = null;
        try {
            conn = Database.instance.getConnection();
            pre = conn.prepareStatement("SELECT gia_tri FROM cau_hinh WHERE khoa = ?");
            pre.setString(1, khoa);
            rs = pre.executeQuery();
            return rs.next() ? rs.getString(1) : null;
        } catch (final Exception e) {
            return null;
        } finally {
            dong(rs, pre, conn);
        }
    }

    public static boolean ghiKhoa(final String khoa, final String giaTri) {
        return ghi("INSERT INTO cau_hinh (khoa, gia_tri, sua_boi, sua_luc) VALUES (?, ?, 'lau_dai', ?)"
                + " ON DUPLICATE KEY UPDATE gia_tri = VALUES(gia_tri), sua_boi = VALUES(sua_boi), sua_luc = VALUES(sua_luc)",
                khoa, giaTri, new Timestamp(com.htbb.pokemon.DongHo.bay())) >= 0;
    }

    /** id -> {xp_truoc, xp_sau} da ghi cua lan {@code lan}; null neu loi. */
    public static java.util.Map<Long, long[]> napNhatKyChuyen(final int lan) {
        final java.util.Map<Long, long[]> m = new java.util.HashMap<Long, long[]>();
        Connection conn = null;
        PreparedStatement pre = null;
        ResultSet rs = null;
        try {
            conn = Database.instance.getConnection();
            pre = conn.prepareStatement("SELECT id, xp_truoc, xp_sau FROM tob_char_xp_chuyen WHERE lan = ?");
            pre.setInt(1, lan);
            rs = pre.executeQuery();
            while (rs.next()) {
                m.put(Long.valueOf(rs.getLong(1)), new long[]{rs.getLong(2), rs.getLong(3)});
            }
            return m;
        } catch (final Exception e) {
            System.out.println("[LAU DAI] LOI doc tob_char_xp_chuyen: " + e);
            return null;
        } finally {
            dong(rs, pre, conn);
        }
    }

    public static boolean ghiNhatKyChuyen(final int lan, final long id, final String huong, final long truoc, final long sau, final long lv) {
        return ghi("INSERT INTO tob_char_xp_chuyen (lan, id, huong, xp_truoc, xp_sau, lv, luc) VALUES (?, ?, ?, ?, ?, ?, ?)",
                Integer.valueOf(lan), Long.valueOf(id), huong, Long.valueOf(truoc), Long.valueOf(sau), Long.valueOf(lv),
                new Timestamp(com.htbb.pokemon.DongHo.bay())) == 1;
    }

    /** UPDATE co dieu kien: chi doi khi xp con dung gia tri truoc. true neu da o gia tri sau. */
    public static boolean apChuyen(final long id, final long truoc, final long sau) {
        final int n = ghi("UPDATE tob_char SET xp = ? WHERE id = ? AND xp = ?", Long.valueOf(sau), Long.valueOf(id), Long.valueOf(truoc));
        return n == 1 || truoc == sau;
    }

    /** Go lich su kien da hen neu con dung moc (tat co). */
    public static boolean goHenSuKien(final String ma, final long mo, final long dongLuc) {
        final int a = ghi("UPDATE tob_su_kien SET mo_luc = NULL, dong_luc = NULL WHERE ma = ? AND mo_luc = ? AND dong_luc = ?",
                ma, new Timestamp(mo), new Timestamp(dongLuc));
        return a == 1;
    }

    /** tob_char_lau_dai co it nhat mot dong (false neu chua co bang). */
    public static boolean coHoSo() {
        Connection conn = null;
        PreparedStatement pre = null;
        ResultSet rs = null;
        try {
            conn = Database.instance.getConnection();
            pre = conn.prepareStatement("SELECT id_char FROM tob_char_lau_dai LIMIT 1");
            rs = pre.executeQuery();
            return rs.next();
        } catch (final Exception e) {
            return false;
        } finally {
            dong(rs, pre, conn);
        }
    }

    /** Ho so cua nhan vat doc THANG CSDL (bo qua co) - cho viec tra nhiem vu ve khi tat co. */
    public static String napHoSoBatKe(final int idChar) {
        return napHoSo(idChar);
    }

    // ------------------------------------------------------------------ ho so nhan vat

    /** Chuoi ho so; "" neu chua co dong; null neu loi CSDL (noi goi KHONG duoc ghi de). */
    public static String napHoSo(final int idChar) {
        Connection conn = null;
        PreparedStatement pre = null;
        ResultSet rs = null;
        try {
            conn = Database.instance.getConnection();
            pre = conn.prepareStatement("SELECT du_lieu FROM tob_char_lau_dai WHERE id_char = ?");
            pre.setInt(1, idChar);
            rs = pre.executeQuery();
            return rs.next() ? rs.getString(1) : "";
        } catch (final Exception e) {
            System.out.println("[LAU DAI] LOI nap ho so " + idChar + ": " + e);
            return null;
        } finally {
            dong(rs, pre, conn);
        }
    }

    public static boolean luuHoSo(final int idChar, final String duLieu) {
        return ghi("INSERT INTO tob_char_lau_dai (id_char, du_lieu, cap_nhat) VALUES (?, ?, ?)"
                + " ON DUPLICATE KEY UPDATE du_lieu = VALUES(du_lieu), cap_nhat = VALUES(cap_nhat)",
                Integer.valueOf(idChar), duLieu, new Timestamp(com.htbb.pokemon.DongHo.bay())) >= 0;
    }

    // ------------------------------------------------------------------ nhiem vu tuan

    /** {ma, tien_do, trang_thai, tuan} cua nhan vat. */
    public static List<Object[]> napNhiemVu(final int idChar) {
        final List<Object[]> kq = new ArrayList<Object[]>();
        Connection conn = null;
        PreparedStatement pre = null;
        ResultSet rs = null;
        try {
            conn = Database.instance.getConnection();
            pre = conn.prepareStatement("SELECT ma, tien_do, trang_thai, tuan FROM tob_nhiem_vu_lau_dai WHERE id_char = ?");
            pre.setInt(1, idChar);
            rs = pre.executeQuery();
            while (rs.next()) {
                kq.add(new Object[]{rs.getString(1), Integer.valueOf(rs.getInt(2)), Integer.valueOf(rs.getInt(3)),
                    Integer.valueOf(rs.getInt(4))});
            }
        } catch (final Exception e) {
            System.out.println("[LAU DAI] LOI nap nhiem vu " + idChar + ": " + e);
            return null;
        } finally {
            dong(rs, pre, conn);
        }
        return kq;
    }

    public static void luuNhiemVu(final int idChar, final String ma, final int tienDo, final int trangThai, final int tuan) {
        ghi("INSERT INTO tob_nhiem_vu_lau_dai (id_char, ma, tien_do, trang_thai, tuan) VALUES (?, ?, ?, ?, ?)"
                + " ON DUPLICATE KEY UPDATE tien_do = VALUES(tien_do), trang_thai = VALUES(trang_thai), tuan = VALUES(tuan)",
                Integer.valueOf(idChar), ma, Integer.valueOf(tienDo), Integer.valueOf(trangThai), Integer.valueOf(tuan));
    }

    /**
     * Danh dau "da nhan thuong" CO DIEU KIEN (chi tu trang thai 1 -> 2). @return true neu
     * chinh lan goi nay doi duoc (muc 5: danh dau truoc, phat thuong sau).
     */
    public static boolean danhDauDaNhan(final int idChar, final String ma) {
        return ghi("UPDATE tob_nhiem_vu_lau_dai SET trang_thai = 2 WHERE id_char = ? AND ma = ? AND trang_thai = 1",
                Integer.valueOf(idChar), ma) == 1;
    }

    // ------------------------------------------------------------------ mua theo tuan

    /** Tang dem mua tuan CO DIEU KIEN (< gioiHan). @return true neu con luot va da tang. */
    public static boolean tangMuaTuan(final int idChar, final int idShop, final int tuan, final int gioiHan) {
        ghi("INSERT IGNORE INTO tob_mua_tuan (id_char, id_shop, tuan, so_luong) VALUES (?, ?, ?, 0)",
                Integer.valueOf(idChar), Integer.valueOf(idShop), Integer.valueOf(tuan));
        return ghi("UPDATE tob_mua_tuan SET so_luong = so_luong + 1 WHERE id_char = ? AND id_shop = ? AND tuan = ? AND so_luong < ?",
                Integer.valueOf(idChar), Integer.valueOf(idShop), Integer.valueOf(tuan), Integer.valueOf(gioiHan)) == 1;
    }

    // ------------------------------------------------------------------ boss / ham nguc / pvp

    public static void ghiDongGop(final long lan, final int idChar, final int phanNghin, final int hang, final int bac) {
        ghi("INSERT INTO tob_boss_dong_gop (lan, id_char, phan_nghin, hang, bac_thuong, ghi_luc) VALUES (?, ?, ?, ?, ?, ?)"
                + " ON DUPLICATE KEY UPDATE phan_nghin = VALUES(phan_nghin), hang = VALUES(hang), bac_thuong = VALUES(bac_thuong)",
                Long.valueOf(lan), Integer.valueOf(idChar), Integer.valueOf(phanNghin), Integer.valueOf(hang),
                Integer.valueOf(bac), new Timestamp(com.htbb.pokemon.DongHo.bay()));
    }

    public static void ghiHamNguc(final int ngay, final int idChar, final int tang) {
        ghi("INSERT INTO tob_ham_nguc (ngay, id_char, tang, ghi_luc) VALUES (?, ?, ?, ?)"
                + " ON DUPLICATE KEY UPDATE tang = GREATEST(tang, VALUES(tang)), ghi_luc = VALUES(ghi_luc)",
                Integer.valueOf(ngay), Integer.valueOf(idChar), Integer.valueOf(tang), new Timestamp(com.htbb.pokemon.DongHo.bay()));
    }

    public static void ghiPvp(final int mua, final int idChar, final int elo, final int thang, final int thua) {
        ghi("INSERT INTO tob_pvp_mua (mua, id_char, elo, thang, thua) VALUES (?, ?, ?, ?, ?)"
                + " ON DUPLICATE KEY UPDATE elo = VALUES(elo), thang = VALUES(thang), thua = VALUES(thua)",
                Integer.valueOf(mua), Integer.valueOf(idChar), Integer.valueOf(elo), Integer.valueOf(thang), Integer.valueOf(thua));
    }

    public static void ghiSuPham(final int idTro, final int idThay, final String moc) {
        ghi("INSERT INTO tob_su_pham (id_tro, id_thay, bat_dau, moc) VALUES (?, ?, ?, ?)"
                + " ON DUPLICATE KEY UPDATE id_thay = VALUES(id_thay), moc = VALUES(moc)",
                Integer.valueOf(idTro), Integer.valueOf(idThay), new Timestamp(com.htbb.pokemon.DongHo.bay()), moc);
    }

    public static void congShiny(final int idChar, final int dex) {
        ghi("INSERT INTO tob_pokedex_shiny (id_char, dex, so_shiny) VALUES (?, ?, 1)"
                + " ON DUPLICATE KEY UPDATE so_shiny = so_shiny + 1", Integer.valueOf(idChar), Integer.valueOf(dex));
    }

    // ------------------------------------------------------------------ nhiem vu (buoc 4)

    /**
     * id_so -> {ma, ngay_mo, canVe (buoc 1 cua chuong chinh), dauChuong (buoc 1 / HD.01)}
     * tu data_quest_lau_dai.
     */
    public static java.util.Map<Integer, Object[]> napMetaNhiemVu() {
        final java.util.Map<Integer, Object[]> m = new java.util.HashMap<Integer, Object[]>();
        Connection conn = null;
        PreparedStatement pre = null;
        ResultSet rs = null;
        try {
            conn = Database.instance.getConnection();
            pre = conn.prepareStatement("SELECT id_so, ma, ngay_mo, loai, buoc FROM data_quest_lau_dai");
            rs = pre.executeQuery();
            while (rs.next()) {
                final String ma = rs.getString(2);
                final String loai = rs.getString(4);
                final String buoc = rs.getString(5);
                final boolean chinh = "chinh".equals(loai);
                final boolean canVe = chinh && "1".equals(buoc);
                final boolean dau = canVe || "HD.01".equals(ma);
                m.put(Integer.valueOf((int) rs.getLong(1)), new Object[]{ma, Integer.valueOf((int) rs.getLong(3)),
                    Boolean.valueOf(canVe), Boolean.valueOf(dau)});
            }
        } catch (final Exception e) {
            System.out.println("[LAU DAI] LOI nap data_quest_lau_dai: " + e);
        } finally {
            dong(rs, pre, conn);
        }
        return m;
    }

    /**
     * Mau cho engine nhiem vu (GD2b): data_quest_lau_dai + data_nhiem_vu_noi_dung (theo ma, doc rieng
     * roi ghep trong Java - khong JOIN). Thieu bang noi dung thi chi co phan co ban.
     */
    public static java.util.LinkedHashMap<String, com.htbb.pokemon.NhiemVuMau> napMauNhiemVu() {
        final java.util.LinkedHashMap<String, com.htbb.pokemon.NhiemVuMau> m = new java.util.LinkedHashMap<String, com.htbb.pokemon.NhiemVuMau>();
        for (final Object[] d : docBang("SELECT ma, loai, ten, ten_npc, cap_toi_thieu, ngay_mo, truoc, kieu_muc_tieu, muc_tieu_id,"
                + " so_luong, lap_lai, exp, xu, luong_khoa, luong, vat_pham, quai_roi, dong_co_tuong_thich FROM data_quest_lau_dai"
                + " ORDER BY id_so", 18)) {
            final com.htbb.pokemon.NhiemVuMau x = new com.htbb.pokemon.NhiemVuMau();
            x.ma = s(d[0]);
            x.loai = s(d[1]);
            x.ten = s(d[2]);
            x.tenNpc = s(d[3]);
            x.cap = so(d[4]);
            x.ngayMo = so(d[5]);
            x.truoc = s(d[6]);
            x.kieu = s(d[7]);
            x.mucTieuId = s(d[8]);
            x.soLuong = Math.max(1, so(d[9]));
            x.lapLai = s(d[10]);
            x.exp = d[11] instanceof Number ? ((Number) d[11]).longValue() : 0L;
            x.xu = d[12] instanceof Number ? ((Number) d[12]).longValue() : 0L;
            x.luongKhoa = so(d[13]);
            x.luong = so(d[14]);
            x.vatPham = s(d[15]);
            x.quaiRoi = s(d[16]);
            // chuoi chinh nam 2 (khong vao duoc data_quest cu): engine chay khi co ban do + tran cap mua (NhiemVuMau.kiemNam2)
            x.chuoiNam2 = com.htbb.pokemon.NhiemVuMau.laChuoiNam2(x.loai, so(d[17]));
            if (com.htbb.pokemon.NhiemVuMau.engineQuanLy(x.loai) || x.chuoiNam2) {
                m.put(x.ma, x);
            }
        }
        for (final Object[] d : docBang("SELECT ma, map_id, toa_do, dieu_kien, muc_tieu_text, hoi_thoai_bat_dau, hoi_thoai_dang_lam,"
                + " hoi_thoai_xong, thuong_khac, nhanh_lua_chon, an, dieu_kien_mo, dap_an, phut FROM data_nhiem_vu_noi_dung", 14)) {
            final com.htbb.pokemon.NhiemVuMau x = m.get(s(d[0]));
            if (x == null) {
                continue;
            }
            x.mapId = s(d[1]).isEmpty() ? -1 : so(d[1]);
            final int[] td = com.htbb.pokemon.NhiemVuMau.docToaDo(s(d[2]));
            if (td != null) {
                x.toaX = td[0];
                x.toaY = td[1];
            }
            x.dieuKien = com.htbb.pokemon.NhiemVuMau.docDieuKien(s(d[3]));
            x.mucTieuText = s(d[4]);
            x.hoiThoaiBatDau = s(d[5]);
            x.hoiThoaiDangLam = s(d[6]);
            x.hoiThoaiXong = s(d[7]);
            x.thuongKhac = s(d[8]);
            x.nhanh = s(d[9]);
            x.an = so(d[10]) == 1;
            x.dieuKienMo = s(d[11]);
            x.dapAn = s(d[12]);
            x.phut = so(d[13]);
        }
        return m;
    }

    /**
     * thuong_khac cua chuoi chinh CU (chinh / huong_dan chay bang data_quest_moi, dong_co_tuong_thich = 1):
     * ma -> thuong_khac (data_nhiem_vu_noi_dung). Phat o NhiemVuEngine.khiXongChuoiCu (Char, luc tra nhiem vu).
     */
    public static java.util.HashMap<String, String> napThuongChuoiCu() {
        final java.util.HashSet<String> cu = new java.util.HashSet<String>();
        for (final Object[] d : docBang("SELECT ma, loai, dong_co_tuong_thich FROM data_quest_lau_dai", 3)) {
            final String loai = s(d[1]);
            if (("chinh".equals(loai) || "huong_dan".equals(loai)) && so(d[2]) != 0) {
                cu.add(s(d[0]));
            }
        }
        final java.util.HashMap<String, String> m = new java.util.HashMap<String, String>();
        for (final Object[] d : docBang("SELECT ma, thuong_khac FROM data_nhiem_vu_noi_dung", 2)) {
            if (cu.contains(s(d[0])) && !s(d[1]).trim().isEmpty()) {
                m.put(s(d[0]), s(d[1]));
            }
        }
        return m;
    }

    /** id -> ten vat pham nhiem vu moi (10..50). */
    public static java.util.TreeMap<Integer, String> napVatPhamNhiemVu() {
        final java.util.TreeMap<Integer, String> m = new java.util.TreeMap<Integer, String>();
        for (final Object[] d : docBang("SELECT id, ten FROM data_vat_pham_nhiem_vu ORDER BY id", 2)) {
            m.put(Integer.valueOf(so(d[0])), s(d[1]));
        }
        return m;
    }

    static String s(final Object o) {
        return o == null ? "" : o.toString();
    }

    /** Kieu muc tieu cua buoc huong dan (engine nhiem vu khong nap dong huong_dan): ma -> {kieu_muc_tieu, muc_tieu_id}. */
    public static java.util.Map<String, String[]> napKieuHuongDan() {
        final java.util.HashMap<String, String[]> m = new java.util.HashMap<String, String[]>();
        for (final Object[] d : docBang("SELECT ma, kieu_muc_tieu, muc_tieu_id FROM data_quest_lau_dai WHERE ma LIKE 'HD.%'", 3)) {
            m.put(s(d[0]), new String[]{s(d[1]), s(d[2])});
        }
        return m;
    }

    /**
     * Chu huong dan tan thu (data_huong_dan_tan_thu, sql/htbb.sql (khoi them_huong_dan_tan_thu.sql)): ma -> {tieu_de, noi_dung}, theo
     * thu_tu. Rong neu chua co bang (khi do HuongDanTanThuGame khong gui gi).
     */
    public static java.util.Map<String, String[]> napHuongDanTanThu() {
        final java.util.LinkedHashMap<String, String[]> m = new java.util.LinkedHashMap<String, String[]>();
        for (final Object[] d : docBang("SELECT ma, tieu_de, noi_dung FROM data_huong_dan_tan_thu ORDER BY thu_tu, ma", 3)) {
            m.put(s(d[0]), new String[]{s(d[1]), s(d[2])});
        }
        return m;
    }

    /** Cac dong nhiem vu TUAN: {ma, ten, cap_toi_thieu, so_luong, exp, xu, luong_khoa}. */
    public static List<Object[]> napNhiemVuTuan() {
        final List<Object[]> kq = new ArrayList<Object[]>();
        Connection conn = null;
        PreparedStatement pre = null;
        ResultSet rs = null;
        try {
            conn = Database.instance.getConnection();
            pre = conn.prepareStatement("SELECT ma, ten, cap_toi_thieu, so_luong, exp, xu, luong_khoa FROM data_quest_lau_dai"
                    + " WHERE loai = ? ORDER BY id_so");
            pre.setString(1, "tuan");
            rs = pre.executeQuery();
            while (rs.next()) {
                kq.add(new Object[]{rs.getString(1), rs.getString(2), Integer.valueOf((int) rs.getLong(3)),
                    Integer.valueOf((int) rs.getLong(4)), Long.valueOf(rs.getLong(5)), Long.valueOf(rs.getLong(6)),
                    Integer.valueOf((int) rs.getLong(7))});
            }
        } catch (final Exception e) {
            System.out.println("[LAU DAI] LOI nap nhiem vu tuan: " + e);
        } finally {
            dong(rs, pre, conn);
        }
        return kq;
    }

    // ------------------------------------------------------------------ vat pham dung (GD2b)

    /** id_item -> {ma, nhom, hieu_ung, gioi_han, cap_tu, cap_den}; rong neu chua co bang. */
    public static java.util.Map<Integer, Object[]> napVatPhamLauDai() {
        final java.util.Map<Integer, Object[]> m = new java.util.HashMap<Integer, Object[]>();
        Connection conn = null;
        PreparedStatement pre = null;
        ResultSet rs = null;
        try {
            conn = Database.instance.getConnection();
            pre = conn.prepareStatement("SELECT id_item, ma, nhom, hieu_ung, gioi_han, cap_tu, cap_den FROM data_vat_pham_lau_dai");
            rs = pre.executeQuery();
            while (rs.next()) {
                m.put(Integer.valueOf(rs.getInt(1)), new Object[]{rs.getString(2), rs.getString(3), rs.getString(4),
                    rs.getString(5), Integer.valueOf(rs.getInt(6)), Integer.valueOf(rs.getInt(7))});
            }
        } catch (final Exception e) {
            System.out.println("[LAU DAI] chua co data_vat_pham_lau_dai: " + e);
        } finally {
            dong(rs, pre, conn);
        }
        return java.util.Collections.unmodifiableMap(m);
    }

    /**
     * Cua hang & doi vat pham (real.CuaHangLauDai): {id, nhom, ten, chi_phi, thuong, cap, ngay_mo, lop,
     * gioi_han_tuan} tu data_cua_hang_lau_dai (tools/luu_tru_sinh_du_lieu/sql_nguon/pokemon_vat_pham_lau_dai.sql). Thieu bang -> rong.
     */
    public static List<Object[]> napCuaHangLauDai() {
        final List<Object[]> kq = new ArrayList<Object[]>();
        for (final Object[] d : docBang("SELECT id, nhom, ten, chi_phi, thuong, cap, ngay_mo, lop, gioi_han_tuan"
                + " FROM data_cua_hang_lau_dai ORDER BY id", 9)) {
            kq.add(new Object[]{Integer.valueOf(so(d[0])), s(d[1]), s(d[2]), s(d[3]), s(d[4]), Integer.valueOf(so(d[5])),
                Integer.valueOf(so(d[6])), Integer.valueOf(so(d[7])), Integer.valueOf(so(d[8]))});
        }
        return kq;
    }

    /** {bac, cong, ten nguyen lieu phu, so} tu data_noi_dung_vat_lieu_nang_cap (doi noi dung). */
    public static List<Object[]> napVatLieuNangCap() {
        final List<Object[]> kq = new ArrayList<Object[]>();
        Connection conn = null;
        PreparedStatement pre = null;
        ResultSet rs = null;
        try {
            conn = Database.instance.getConnection();
            pre = conn.prepareStatement("SELECT bac, cong, nguyen_lieu_phu, so_nl_phu FROM data_noi_dung_vat_lieu_nang_cap");
            rs = pre.executeQuery();
            while (rs.next()) {
                kq.add(new Object[]{Integer.valueOf((int) rs.getLong(1)), Integer.valueOf((int) rs.getLong(2)),
                    rs.getString(3) == null ? "" : rs.getString(3), Integer.valueOf((int) rs.getLong(4))});
            }
        } catch (final Exception e) {
            System.out.println("[LAU DAI] chua co data_noi_dung_vat_lieu_nang_cap: " + e);
        } finally {
            dong(rs, pre, conn);
        }
        return kq;
    }

    // ------------------------------------------------------------------ noi dung (GD2b)

    /** Doc mot SELECT thanh danh sach dong Object[] (cot theo thu tu); rong neu loi / chua co bang. */
    static List<Object[]> docBang(final String sql, final int soCot) {
        final List<Object[]> kq = new ArrayList<Object[]>();
        Connection conn = null;
        PreparedStatement pre = null;
        ResultSet rs = null;
        try {
            conn = Database.instance.getConnection();
            pre = conn.prepareStatement(sql);
            rs = pre.executeQuery();
            while (rs.next()) {
                final Object[] d = new Object[soCot];
                for (int i = 0; i < soCot; i++) {
                    d[i] = rs.getObject(i + 1);
                }
                kq.add(d);
            }
        } catch (final Exception e) {
            System.out.println("[LAU DAI] chua doc duoc (" + sql.substring(0, Math.min(50, sql.length())) + "): " + e);
        } finally {
            dong(rs, pre, conn);
        }
        return kq;
    }

    static int so(final Object o) {
        if (o == null) {
            return 0;
        }
        if (o instanceof Number) {
            return ((Number) o).intValue();
        }
        try {
            return Integer.parseInt(o.toString().trim());
        } catch (final NumberFormatException e) {
            return 0;
        }
    }

    /** {nguon, khoa_nguon, loai, id, ppm, sl_min, sl_max, khoa}. */
    public static List<Object[]> napRoiNoiDung() {
        final List<Object[]> kq = new ArrayList<Object[]>();
        for (final Object[] d : docBang("SELECT nguon, khoa_nguon, vat_pham, ppm, sl_min, sl_max, khoa FROM data_noi_dung_roi", 7)) {
            final String vp = String.valueOf(d[2]);
            final int i = vp.indexOf(':');
            if (i <= 0) {
                continue;
            }
            kq.add(new Object[]{String.valueOf(d[0]), String.valueOf(d[1]), vp.substring(0, i), Integer.valueOf(so(vp.substring(i + 1))),
                Integer.valueOf(so(d[3])), Integer.valueOf(Math.max(1, so(d[4]))), Integer.valueOf(Math.max(1, so(d[5]))), Integer.valueOf(so(d[6]))});
        }
        return kq;
    }

    /** id goc -> {hp, tan cong, nhan roi}. */
    public static java.util.HashMap<Integer, int[]> napAlpha() {
        final java.util.HashMap<Integer, int[]> m = new java.util.HashMap<Integer, int[]>();
        for (final Object[] d : docBang("SELECT id_goc, hp, tan_cong, nhan_roi FROM data_noi_dung_quai_tinh_anh", 4)) {
            m.put(Integer.valueOf(so(d[0])), new int[]{so(d[1]), so(d[2]), so(d[3])});
        }
        return m;
    }

    /** cap_tu cua dai -> template nam 2 (tu chuoi "144:Carvanha(84)|..."). */
    public static java.util.HashMap<Integer, int[]> napNam2() {
        final java.util.HashMap<Integer, int[]> m = new java.util.HashMap<Integer, int[]>();
        for (final Object[] d : docBang("SELECT cap_tu, template_nam2 FROM data_noi_dung_quai_dai_cap", 2)) {
            final String s = d[1] == null ? "" : d[1].toString();
            if (s.isEmpty()) {
                continue;
            }
            final List<Integer> ds = new ArrayList<Integer>();
            for (final String phan : s.split("\\|")) {
                final int i = phan.indexOf(':');
                if (i > 0) {
                    ds.add(Integer.valueOf(so(phan.substring(0, i))));
                }
            }
            final int[] a = new int[ds.size()];
            for (int i = 0; i < a.length; i++) {
                a[i] = ds.get(i).intValue();
            }
            m.put(Integer.valueOf(so(d[0])), a);
        }
        return m;
    }

    /** tuan -> {template nam 1, template nam 2}. */
    public static java.util.HashMap<Integer, int[]> napBossTuan() {
        final java.util.HashMap<Integer, int[]> m = new java.util.HashMap<Integer, int[]>();
        for (final Object[] d : docBang("SELECT tuan, template_quai, template_nam2 FROM data_noi_dung_boss_the_gioi", 3)) {
            m.put(Integer.valueOf(so(d[0])), new int[]{so(d[1]), so(d[2])});
        }
        return m;
    }

    /** Dong Gym: {stt, ma_nhiem_vu, huy_hieu, cap, template_quai, ten_boss, hp, tan_cong, phong_thu, no_sau_phut, thuong_lan_dau, thuong_tuan}. */
    public static List<Object[]> napGym() {
        return docBang("SELECT stt, ma_nhiem_vu, huy_hieu, cap, template_quai, ten_boss, hp, tan_cong, phong_thu, no_sau_phut,"
                + " thuong_lan_dau, thuong_tuan FROM data_noi_dung_gym ORDER BY stt", 12);
    }

    /** Chieu moi: {dong, so_hieu, cap_moi_cap_chieu, phan_tram_cap1, phan_tram_cap10, so_don, hoi_chieu_giay}. */
    public static List<Object[]> napChieu() {
        return docBang("SELECT dong, so_hieu, cap_moi_cap_chieu, phan_tram_cap1, phan_tram_cap10, so_don, hoi_chieu_giay"
                + " FROM data_noi_dung_chieu", 7);
    }

    // ------------------------------------------------------------------ quai (6.2)

    /** id -> {maxhp, attack, defent, rcvxp, rcvgold} tu data_monster_lau_dai; rong neu loi. */
    public static java.util.Map<Integer, long[]> napChiSoQuai() {
        final java.util.Map<Integer, long[]> m = new java.util.HashMap<Integer, long[]>();
        Connection conn = null;
        PreparedStatement pre = null;
        ResultSet rs = null;
        try {
            conn = Database.instance.getConnection();
            pre = conn.prepareStatement("SELECT id, maxhp, attack, defent, rcvxp, rcvgold FROM data_monster_lau_dai");
            rs = pre.executeQuery();
            while (rs.next()) {
                m.put(Integer.valueOf(rs.getInt(1)), new long[]{rs.getLong(2), rs.getLong(3), rs.getLong(4), rs.getLong(5), rs.getLong(6)});
            }
            System.out.println("[LAU DAI] chi so quai moi: " + m.size() + " mau.");
        } catch (final Exception e) {
            System.out.println("[LAU DAI] LOI nap data_monster_lau_dai: " + e);
        } finally {
            dong(rs, pre, conn);
        }
        return m;
    }

    // ------------------------------------------------------------------ shop (2.10)

    /** id_shop -> {gia_moi, tien_cu, tien_moi, gioi_han_tuan}; rong neu chua nap / loi. */
    public static volatile java.util.Map<Integer, int[]> GIA_SHOP = java.util.Collections.emptyMap();

    /** Nap data_shop_gia_lau_dai (goi tu Database.loadShopTemplate khi bat co). */
    public static java.util.Map<Integer, int[]> napGiaShop() {
        final java.util.Map<Integer, int[]> m = new java.util.HashMap<Integer, int[]>();
        Connection conn = null;
        PreparedStatement pre = null;
        ResultSet rs = null;
        try {
            conn = Database.instance.getConnection();
            pre = conn.prepareStatement("SELECT id_shop, gia_moi, tien_cu, tien_moi, gioi_han_tuan FROM data_shop_gia_lau_dai");
            rs = pre.executeQuery();
            while (rs.next()) {
                m.put(Integer.valueOf(rs.getInt(1)), new int[]{(int) Math.min(Integer.MAX_VALUE, rs.getLong(2)),
                    rs.getInt(3), rs.getInt(4), rs.getInt(5)});
            }
        } catch (final Exception e) {
            System.out.println("[LAU DAI] LOI nap data_shop_gia_lau_dai: " + e);
        } finally {
            dong(rs, pre, conn);
        }
        GIA_SHOP = java.util.Collections.unmodifiableMap(m);
        return GIA_SHOP;
    }

    /**
     * Chan mua o shop dac biet (doBuySpecialItem) khi bat co. @return null neu duoc mua, nguoc
     * lai ly do. Mon doi tien (vd luong -> luong khoa) bi chan o shop luong (Q10: luong khong mua
     * suc manh); gioi han tuan duoc dem co dieu kien TRUOC khi phat (muc 5).
     */
    /** Mon shop luong chuyen sang tra bang luong khoa (tien_moi = 2), xem doBuySpecialItem. */
    public static boolean traBangLuongKhoa(final int idShop) {
        final int[] g = GIA_SHOP.get(Integer.valueOf(idShop));
        return g != null && g[2] == 2 && g[1] != 2;
    }

    public static String chanMuaShop(final int idChar, final int idShop, final int tuan) {
        final int[] g = GIA_SHOP.get(Integer.valueOf(idShop));
        if (g == null) {
            return null;
        }
        if (g[2] == 9) {
            return "Vật phẩm này đã ngừng bán.";
        }
        if (g[2] != g[1] && g[2] != 2) {
            return "Vật phẩm này không còn bán bằng lượng (chỉ đổi bằng lượng khoá).";
        }
        if (g[3] > 0 && !tangMuaTuan(idChar, idShop, tuan, g[3])) {
            return "Đã mua đủ " + g[3] + " lần trong tuần này.";
        }
        return null;
    }

    // ------------------------------------------------------------------ qua hoat dong (tai khoan)

    /** {ngay, giay, moc, chuoi_buoc, chuoi_ngay, thang, ngay_thang, thang_da_thuong}; null neu loi. */
    public static int[] napQuaHoatDong(final int userId) {
        Connection conn = null;
        PreparedStatement pre = null;
        ResultSet rs = null;
        try {
            conn = Database.instance.getConnection();
            pre = conn.prepareStatement("SELECT ngay, giay, moc, chuoi_buoc, chuoi_ngay, thang, ngay_thang, thang_da_thuong"
                    + " FROM tob_qua_online WHERE user_id = ?");
            pre.setInt(1, userId);
            rs = pre.executeQuery();
            final int[] d = new int[8];
            if (rs.next()) {
                for (int i = 0; i < 8; i++) {
                    d[i] = rs.getInt(i + 1);
                }
            }
            return d;
        } catch (final Exception e) {
            System.out.println("[LAU DAI] LOI doc tob_qua_online: " + e);
            return null;
        } finally {
            dong(rs, pre, conn);
        }
    }

    public static void luuQuaHoatDong(final int userId, final int[] d) {
        final int[] c;
        synchronized (d) {
            c = d.clone();
        }
        ghi("INSERT INTO tob_qua_online (user_id, ngay, giay, moc, chuoi_buoc, chuoi_ngay, thang, ngay_thang, thang_da_thuong)"
                + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE ngay = VALUES(ngay), giay = VALUES(giay),"
                + " moc = VALUES(moc), chuoi_buoc = VALUES(chuoi_buoc), chuoi_ngay = VALUES(chuoi_ngay), thang = VALUES(thang),"
                + " ngay_thang = VALUES(ngay_thang), thang_da_thuong = VALUES(thang_da_thuong)",
                Integer.valueOf(userId), Integer.valueOf(c[0]), Integer.valueOf(c[1]), Integer.valueOf(c[2]), Integer.valueOf(c[3]),
                Integer.valueOf(c[4]), Integer.valueOf(c[5]), Integer.valueOf(c[6]), Integer.valueOf(c[7]));
    }

    public static void ghiChiemThanh(final long lan, final int idChar, final int idClan, final long diem, final String thuong) {
        ghi("INSERT INTO tob_chiem_thanh_tuan (lan, id_char, id_clan, diem, thuong, luc) VALUES (?, ?, ?, ?, ?, ?)"
                + " ON DUPLICATE KEY UPDATE diem = VALUES(diem), thuong = VALUES(thuong)", Long.valueOf(lan), Integer.valueOf(idChar),
                Integer.valueOf(idClan), Long.valueOf(diem), thuong, new Timestamp(com.htbb.pokemon.DongHo.bay()));
    }

    // ------------------------------------------------------------------ giftcode an toan

    /** {phan_thuong, han ms (0 = khong han), tong_luot, da_dung, cap_toi_thieu, bat}; null neu khong co / loi. */
    public static Object[] napGiftcode(final String ma) {
        Connection conn = null;
        PreparedStatement pre = null;
        ResultSet rs = null;
        try {
            conn = Database.instance.getConnection();
            pre = conn.prepareStatement("SELECT phan_thuong, han, tong_luot, da_dung, cap_toi_thieu, bat FROM tob_giftcode WHERE ma = ?");
            pre.setString(1, ma);
            rs = pre.executeQuery();
            if (!rs.next()) {
                return null;
            }
            final Timestamp han = rs.getTimestamp(2);
            return new Object[]{rs.getString(1), Long.valueOf(han == null ? 0L : han.getTime()), Integer.valueOf(rs.getInt(3)),
                Integer.valueOf(rs.getInt(4)), Integer.valueOf(rs.getInt(5)), Integer.valueOf(rs.getInt(6))};
        } catch (final Exception e) {
            System.out.println("[LAU DAI] LOI doc tob_giftcode: " + e);
            return null;
        } finally {
            dong(rs, pre, conn);
        }
    }

    /** Tong gia tri giftcode da phat trong thang yyyyMM; -1 neu loi. */
    public static long tongGiftcodeThang(final int thang) {
        Connection conn = null;
        PreparedStatement pre = null;
        ResultSet rs = null;
        try {
            conn = Database.instance.getConnection();
            pre = conn.prepareStatement("SELECT SUM(gia_tri) FROM tob_giftcode_log WHERE thang = ?");
            pre.setInt(1, thang);
            rs = pre.executeQuery();
            return rs.next() ? rs.getLong(1) : 0L;
        } catch (final Exception e) {
            return -1L;
        } finally {
            dong(rs, pre, conn);
        }
    }

    /** INSERT log (khoa chinh ma+user_id): true = lan dau cua tai khoan. */
    public static boolean ghiLogGiftcode(final String ma, final int userId, final int idChar, final String ten,
            final String thuong, final int giaTri, final int thang) {
        return ghi("INSERT IGNORE INTO tob_giftcode_log (ma, user_id, id_char, charname, phan_thuong, gia_tri, thang, luc)"
                + " VALUES (?, ?, ?, ?, ?, ?, ?, ?)", ma, Integer.valueOf(userId), Integer.valueOf(idChar), ten, thuong,
                Integer.valueOf(giaTri), Integer.valueOf(thang), new Timestamp(com.htbb.pokemon.DongHo.bay())) == 1;
    }

    public static boolean truLuotGiftcode(final String ma) {
        return ghi("UPDATE tob_giftcode SET da_dung = da_dung + 1 WHERE ma = ? AND da_dung < tong_luot AND bat = 1", ma) == 1;
    }

    public static void xoaLogGiftcode(final String ma, final int userId) {
        ghi("DELETE FROM tob_giftcode_log WHERE ma = ? AND user_id = ?", ma, Integer.valueOf(userId));
    }

    // ------------------------------------------------------------------ lich su kien (5.2)

    /** Hen mo/dong cho ma {@code ma} neu ca hai cot dang trong. @return true neu da hen. */
    public static boolean henSuKien(final String ma, final long mo, final long dong) {
        return ghi("UPDATE tob_su_kien SET mo_luc = ?, dong_luc = ? WHERE ma = ? AND mo_luc IS NULL AND dong_luc IS NULL",
                new Timestamp(mo), new Timestamp(dong), ma) == 1;
    }

    // ------------------------------------------------------------------ chi so kinh te (2.5)

    /** Cap cua nguoi hoat dong 7 ngay (de tinh trung vi cho bat kip). */
    public static int capTrungVi() {
        final ChiSoKinhTe c = tinhChiSo();
        return c == null ? 0 : c.capTrungVi;
    }

    /** Doc tob_char (nguoi hoat dong 7 ngay) va tinh chi so; null neu loi CSDL. */
    public static ChiSoKinhTe tinhChiSo() {
        Connection conn = null;
        PreparedStatement pre = null;
        ResultSet rs = null;
        final List<long[]> ds = new ArrayList<long[]>();
        try {
            conn = Database.instance.getConnection();
            pre = conn.prepareStatement("SELECT lastLv, gold, kho_xu, luong, kho_luong, luonglock, kho_luong_khoa"
                    + " FROM tob_char WHERE lastLog >= ?");
            pre.setTimestamp(1, new Timestamp(com.htbb.pokemon.DongHo.bay() - 7L * 86400000L));
            rs = pre.executeQuery();
            while (rs.next()) {
                ds.add(new long[]{rs.getLong(1), rs.getLong(2) + rs.getLong(3), rs.getLong(4) + rs.getLong(5),
                    rs.getLong(6) + rs.getLong(7)});
            }
        } catch (final Exception e) {
            System.out.println("[LAU DAI] LOI doc chi so kinh te: " + e);
            return null;
        } finally {
            dong(rs, pre, conn);
        }
        final int[] cap = new int[ds.size()];
        final long[] xu = new long[ds.size()];
        final long[] luong = new long[ds.size()];
        final long[] lk = new long[ds.size()];
        for (int i = 0; i < ds.size(); i++) {
            cap[i] = (int) ds.get(i)[0];
            xu[i] = ds.get(i)[1];
            luong[i] = ds.get(i)[2];
            lk[i] = ds.get(i)[3];
        }
        return ChiSoKinhTe.tinh(cap, xu, luong, lk);
    }

    /** Job dem: tinh va ghi dong cua hom nay (ghi de neu da co). @return chi so da ghi, null neu loi. */
    public static ChiSoKinhTe ghiNgay() {
        final ChiSoKinhTe c = tinhChiSo();
        if (c == null) {
            return null;
        }
        final long[] vaoRa = LauDai.layVaDatLaiXu();
        ghi("INSERT INTO tob_kinh_te_ngay (ngay, so_nguoi, cap_trung_vi, chi_so_gia, tong_xu, tong_luong, tong_luong_khoa,"
                + " cung_tien_thuc_x100, top1_phan_tram_x100, xu_vao, xu_ra, muc, ghi_luc) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
                + " ON DUPLICATE KEY UPDATE so_nguoi = VALUES(so_nguoi), cap_trung_vi = VALUES(cap_trung_vi),"
                + " chi_so_gia = VALUES(chi_so_gia), tong_xu = VALUES(tong_xu), tong_luong = VALUES(tong_luong),"
                + " tong_luong_khoa = VALUES(tong_luong_khoa), cung_tien_thuc_x100 = VALUES(cung_tien_thuc_x100),"
                + " top1_phan_tram_x100 = VALUES(top1_phan_tram_x100), xu_vao = xu_vao + VALUES(xu_vao),"
                + " xu_ra = xu_ra + VALUES(xu_ra), muc = VALUES(muc), ghi_luc = VALUES(ghi_luc)",
                new java.sql.Date(com.htbb.pokemon.DongHo.bay()), Integer.valueOf(c.soNguoi), Integer.valueOf(c.capTrungVi),
                Long.valueOf(c.chiSoGia), Long.valueOf(c.tongXu), Long.valueOf(c.tongLuong), Long.valueOf(c.tongLuongKhoa),
                Long.valueOf(c.cungTienThucX100), Integer.valueOf(c.phanTramTop1X100), Long.valueOf(vaoRa[0]),
                Long.valueOf(vaoRa[1]), Integer.valueOf(c.muc), new Timestamp(com.htbb.pokemon.DongHo.bay()));
        return c;
    }

    // ------------------------------------------------------------------ chuyen EXP (buoc 3)

    /**
     * Da chuyen XONG chua: dong moc id = 0 (tob_char.id bat dau tu 1) chi duoc ghi SAU khi
     * moi nhan vat da chuyen. 1 = xong, 0 = chua, -1 = loi CSDL.
     */
    public static int daChuyenExpXong() {
        Connection conn = null;
        PreparedStatement pre = null;
        ResultSet rs = null;
        try {
            conn = Database.instance.getConnection();
            pre = conn.prepareStatement("SELECT COUNT(*) FROM tob_char_xp_cu WHERE id = 0");
            rs = pre.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;
        } catch (final Exception e) {
            return -1;
        } finally {
            dong(rs, pre, conn);
        }
    }

    /** {id, xp, lastLv} moi nhan vat. */
    public static List<long[]> napXpTatCa() {
        final List<long[]> kq = new ArrayList<long[]>();
        Connection conn = null;
        PreparedStatement pre = null;
        ResultSet rs = null;
        try {
            conn = Database.instance.getConnection();
            pre = conn.prepareStatement("SELECT id, xp, lastLv FROM tob_char");
            rs = pre.executeQuery();
            while (rs.next()) {
                kq.add(new long[]{rs.getLong(1), rs.getLong(2), rs.getLong(3)});
            }
            return kq;
        } catch (final Exception e) {
            System.out.println("[LAU DAI] LOI doc xp: " + e);
            return null;
        } finally {
            dong(rs, pre, conn);
        }
    }

    /** Ghi dong moc "da chuyen xong" (id = 0). */
    public static boolean danhDauChuyenXong() {
        return ghi("INSERT IGNORE INTO tob_char_xp_cu (id, xp, lastLv, xp_moi) VALUES (0, 0, 0, 0)") >= 0;
    }

    /**
     * Sao luu (INSERT IGNORE) roi ghi xp moi. Da co dong sao luu (lan chay truoc bi ngat giua
     * chung) thi KHONG chuyen lan 2 - tra false.
     */
    public static boolean chuyenXp(final long id, final long xpCu, final long lastLv, final long xpMoi) {
        final int a = ghi("INSERT IGNORE INTO tob_char_xp_cu (id, xp, lastLv, xp_moi) VALUES (?, ?, ?, ?)",
                Long.valueOf(id), Long.valueOf(xpCu), Long.valueOf(lastLv), Long.valueOf(xpMoi));
        if (a != 1) {
            return false; // da chuyen roi (hoac loi): KHONG chuyen lan 2
        }
        return ghi("UPDATE tob_char SET xp = ? WHERE id = ? AND xp = ?", Long.valueOf(xpMoi), Long.valueOf(id),
                Long.valueOf(xpCu)) == 1;
    }

    // ------------------------------------------------------------------ chung

    /** executeUpdate voi tham so bind; -1 neu loi. */
    /** {@link #ghi} cho lop ngoai goi (real.XepHangPvpGame): so dong da doi, -1 = loi. */
    /** real.LangKhoiDau: nhan vat co dong tob_lang_khoi_dau (tao tu ban sua lang theo vung). */
    public static boolean coLangKhoiDau(final int idChar) {
        return !docBang("SELECT id_char FROM tob_lang_khoi_dau WHERE id_char = " + idChar, 1).isEmpty();
    }

    /**
     * real.KhuVucNhiemVuGame: vung dich cua buoc nhiem vu dia diem (data_nhiem_vu_khu_vuc, chi dong bat = 1):
     * {ma, map_id, x1, y1, x2, y2, noi_chuyen_sau, ten_noi}. Rong neu chua co bang.
     */
    public static List<Object[]> napKhuVucNhiemVu() {
        return docBang("SELECT ma, map_id, x1, y1, x2, y2, noi_chuyen_sau, ten_noi FROM data_nhiem_vu_khu_vuc WHERE bat = 1"
                + " ORDER BY ma, stt", 8);
    }

    /**
     * real.HanhDongNhiemVuGame: buoc noi chuyen (-64 type 3) co muc tieu hanh dong that - dong data_nhiem_vu_noi_dung co
     * {@code kiem_hanh_dong=1} trong dieu_kien (khoi sua_buoc_hanh_dong.sql) ghep kieu / muc tieu / so luong cua
     * data_quest_lau_dai (doc rieng, ghep trong Java). Rong neu chua chay khoi SQL (khi do moi buoc giu nhu cu).
     */
    public static java.util.Map<String, com.htbb.pokemon.BuocHanhDong> napBuocHanhDong() {
        final java.util.Map<String, String> dk = new java.util.HashMap<String, String>();
        for (final Object[] d : docBang("SELECT ma, dieu_kien FROM data_nhiem_vu_noi_dung WHERE dieu_kien LIKE '%"
                + com.htbb.pokemon.BuocHanhDong.DAU + "=1%'", 2)) {
            dk.put(s(d[0]), s(d[1]));
        }
        final java.util.Map<String, com.htbb.pokemon.BuocHanhDong> m = new java.util.HashMap<String, com.htbb.pokemon.BuocHanhDong>();
        if (dk.isEmpty()) {
            return m;
        }
        for (final Object[] d : docBang("SELECT ma, kieu_muc_tieu, muc_tieu_id, so_luong FROM data_quest_lau_dai"
                + " WHERE loai IN ('chinh', 'phu')", 4)) {
            final String ma = s(d[0]);
            final com.htbb.pokemon.BuocHanhDong b = com.htbb.pokemon.BuocHanhDong.tao(ma, s(d[1]), s(d[2]), so(d[3]), dk.get(ma));
            if (b != null) {
                m.put(ma, b);
            }
        }
        return m;
    }

    public static int ghiCong(final String sql, final Object... thamSo) {
        return ghi(sql, thamSo);
    }

    static int ghi(final String sql, final Object... thamSo) {
        Connection conn = null;
        PreparedStatement pre = null;
        try {
            conn = Database.instance.getConnection();
            pre = conn.prepareStatement(sql);
            for (int i = 0; i < thamSo.length; i++) {
                pre.setObject(i + 1, thamSo[i]);
            }
            return pre.executeUpdate();
        } catch (final Exception e) {
            System.out.println("[LAU DAI] LOI SQL: " + e + " <- " + sql.substring(0, Math.min(60, sql.length())));
            return -1;
        } finally {
            dong(null, pre, conn);
        }
    }

    private static void dong(final ResultSet rs, final PreparedStatement pre, final Connection conn) {
        try {
            if (rs != null) {
                rs.close();
            }
        } catch (final Exception ignored) {
        }
        try {
            if (pre != null) {
                pre.close();
            }
        } catch (final Exception ignored) {
        }
        try {
            if (conn != null) {
                Database.connPool.free(conn);
            }
        } catch (final Exception ignored) {
        }
    }
}
