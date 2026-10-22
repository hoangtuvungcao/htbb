package data;

import com.htbb.pokemon.LoaiDuLieu;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Nap bang loai day du tu CSDL (sql/them_loai_day_du.sql): data_loai (+ data_loai_sprite), data_loai_tien_hoa,
 * data_loai_vung, data_loai_khu_vuc_map. Thieu bang (CSDL chua chay tep SQL) -> tra null (noi goi giu bang cu / rong).
 */
public final class DatabaseLoai {

    private DatabaseLoai() {
    }

    interface Dong {
        void doc(ResultSet rs) throws Exception;
    }

    private static boolean doc(final Connection c, final String sql, final Dong f) {
        try (PreparedStatement p = c.prepareStatement(sql); ResultSet rs = p.executeQuery()) {
            while (rs.next()) {
                f.doc(rs);
            }
            return true;
        } catch (final Exception e) {
            System.out.println("[LOAI] khong doc duoc (" + sql.substring(0, Math.min(50, sql.length())) + "): " + e);
            return false;
        }
    }

    /** Bang moi, hoac null neu CSDL chua co data_loai. */
    public static LoaiDuLieu nap() {
        if (Database.instance == null) {
            return null;
        }
        Connection c = null;
        try {
            c = Database.instance.getConnection();
            final List<LoaiDuLieu.Loai> loai = new ArrayList<LoaiDuLieu.Loai>();
            final boolean ok = doc(c, "SELECT l.dex, l.ten, l.the_he, l.he1, l.he2, l.hp, l.tan_cong, l.phong_thu, l.tan_cong_db,"
                    + " l.phong_thu_db, l.toc_do, l.nhom, l.dex_goc, l.tien_hoa_tu, l.giai_doan, l.bac, l.goc_bat, l.bat_duoc,"
                    + " l.chi_su_kien, l.bat, IFNULL(s.trang_thai, 0), IFNULL(s.chan_dung, 0), IFNULL(s.goi_may_chu, 0),"
                    + " IFNULL(s.trong_jar, 0)"
                    + " FROM data_loai l LEFT JOIN data_loai_sprite s ON s.dex = l.dex ORDER BY l.dex", (rs) -> loai.add(
                    new LoaiDuLieu.Loai(rs.getInt(1), rs.getString(2), rs.getInt(3), rs.getInt(4), rs.getInt(5),
                            new int[]{rs.getInt(6), rs.getInt(7), rs.getInt(8), rs.getInt(9), rs.getInt(10), rs.getInt(11)},
                            rs.getInt(12), rs.getInt(13), rs.getInt(14), rs.getInt(15), rs.getInt(16), rs.getInt(17),
                            rs.getInt(18) != 0, rs.getInt(19) != 0, rs.getInt(20) != 0, rs.getInt(21), rs.getInt(22) != 0,
                            rs.getInt(23) != 0, rs.getInt(24) != 0)));
            if (!ok || loai.isEmpty()) {
                return null;
            }
            final List<LoaiDuLieu.TienHoa> th = new ArrayList<LoaiDuLieu.TienHoa>();
            doc(c, "SELECT tu_dex, sang_dex, kieu, co_che, cap, ten_vat_pham, id_vat_pham, ngay_dem, than_thiet, keo, dieu_kien"
                    + " FROM data_loai_tien_hoa ORDER BY tu_dex, sang_dex", (rs) -> th.add(new LoaiDuLieu.TienHoa(rs.getInt(1),
                    rs.getInt(2), rs.getString(3), rs.getString(4), rs.getInt(5), rs.getString(6), rs.getInt(7), rs.getInt(8),
                    rs.getInt(9), rs.getInt(10), rs.getString(11))));
            final List<LoaiDuLieu.Vung> vung = new ArrayList<LoaiDuLieu.Vung>();
            doc(c, "SELECT dex, khu_vuc, vung, cap_min, cap_max, trong_so, ngay_dem, thoi_tiet, bac_gap FROM data_loai_vung"
                    + " ORDER BY khu_vuc, vung, dex", (rs) -> vung.add(new LoaiDuLieu.Vung(rs.getInt(1), rs.getString(2),
                    rs.getInt(3), rs.getInt(4), rs.getInt(5), rs.getInt(6), rs.getInt(7), rs.getInt(8), rs.getInt(9))));
            final Map<Integer, String> km = new HashMap<Integer, String>();
            doc(c, "SELECT map_id, khu_vuc FROM data_loai_khu_vuc_map", (rs) -> km.put(rs.getInt(1), rs.getString(2)));
            return new LoaiDuLieu(loai, th, vung, km);
        } catch (final Exception e) {
            System.out.println("[LOAI] nap loi: " + e);
            return null;
        } finally {
            try {
                if (c != null) {
                    c.close();
                }
            } catch (final Exception ignored) {
            }
        }
    }
}
