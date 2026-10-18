package server.quantri;

import data.Database;
import java.awt.Component;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import real.CharManager;

/**
 * "Quan Ly Gem" cua bang dieu khien cu - giu NGUYEN hai chuc nang va cung cau lenh:
 *   - Kiem tra ngoc: Database.checkDetailGems(...) -> tep check_gem.txt;
 *   - Thu hoi ngoc (nhan vat OFFLINE): tru so luong trong tob_gem_new (soluong / slock).
 * Them: xac nhan truoc khi thu hoi, chay ngoai EDT, ghi kiem toan.
 */
final class CongCuNgoc {

    private CongCuNgoc() {
    }

    static void hien(final Component cha, final server.AdminPanel khung) {
        final int chon = HopThoai.hien(cha, "Công cụ ngọc (gem)", "Kiểm tra số lượng ngọc (xuất ra tệp kết quả) hoặc thu hồi ngọc"
                + " của Pokémon đang offline.", null, false, "Kiểm tra ngọc", "Thu hồi ngọc", "Đóng");
        if (chon == 0) {
            kiem(cha);
        } else if (chon == 1) {
            thuHoi(cha);
        }
    }

    static void kiem(final Component cha) {
        final ThanhPhan.ONhap ten = new ThanhPhan.ONhap("", 18).goiY("để trống = mọi Pokémon");
        final ThanhPhan.ONhap ids = new ThanhPhan.ONhap("", 18).goiY("để trống = tất cả");
        final ThanhPhan.ONhap sl = new ThanhPhan.ONhap("", 18);
        final ButtonGroup g = new ButtonGroup();
        final JPanel loai = TrangNguoiChoi.nhomRadio(g, "Gem mở", "Gem khoá", "Cả hai");
        final JComboBox<String> so = new JComboBox<String>(new String[]{"Lớn hơn hoặc bằng", "Nhỏ hơn hoặc bằng", "Bằng"});
        if (!HopThoai.bieuMau(cha, "Kiểm tra ngọc", null, HopThoai.luoi("Tên Pokémon", ten, "ID ngọc", ids, "Số lượng", sl,
                "Loại", loai, "Điều kiện", so), "Kiểm tra")) {
            return;
        }
        final String q = sl.getText().trim();
        try {
            Integer.parseInt(q);
        } catch (final NumberFormatException e) {
            HopThoai.ketQua(cha, "Sai số lượng", "Số lượng phải là số nguyên.");
            return;
        }
        final String cmp = new String[]{"greater", "less", "equal"}[so.getSelectedIndex()];
        final String kieu = new String[]{"soluong", "slock", "both"}[Integer.parseInt(g.getSelection().getActionCommand())];
        HopThoai.chay(cha, null, () -> {
            try {
                Database.checkDetailGems(ids.getText().trim(), q, cmp, ten.getText().trim(), "check_gem", kieu);
                KiemToanQuanTri.ghi("KIEM_NGOC", ten.getText().trim(), "ids=" + ids.getText().trim() + " " + cmp + " " + q + " " + kieu);
                return "Đã kiểm tra ngọc - kết quả ghi trong tệp check_gem.txt ở thư mục máy chủ.";
            } catch (final Exception e) {
                return "Lỗi khi kiểm tra ngọc: " + e.getMessage();
            }
        }, null);
    }

    static void thuHoi(final Component cha) {
        final ThanhPhan.ONhap ten = new ThanhPhan.ONhap("", 18);
        final ThanhPhan.ONhap ids = new ThanhPhan.ONhap("", 18).goiY("vd 12,15");
        final ThanhPhan.ONhap sl = new ThanhPhan.ONhap("", 18).goiY("vd 1,3 (cùng thứ tự)");
        final ButtonGroup g = new ButtonGroup();
        final JPanel loai = TrangNguoiChoi.nhomRadio(g, "Gem mở", "Gem khoá");
        if (!HopThoai.bieuMau(cha, "Thu hồi ngọc", "Chỉ cho Pokémon OFFLINE (bản trong RAM sẽ ghi đè khi lưu).",
                HopThoai.luoi("Tên Pokémon", ten, "ID ngọc", ids, "Số lượng", sl, "Loại", loai), "Tiếp tục")) {
            return;
        }
        final String t = ten.getText().trim();
        final String[] id = ids.getText().trim().split(",");
        final String[] so = sl.getText().trim().split(",");
        final boolean khoa = "1".equals(g.getSelection().getActionCommand());
        if (t.isEmpty() || id.length != so.length) {
            HopThoai.ketQua(cha, "Thiếu thông tin", "Cần tên Pokémon; số ID và số lượng phải khớp nhau.");
            return;
        }
        HopThoai.chay(cha, "Thu hồi " + (khoa ? "gem khoá" : "gem mở") + " " + ids.getText().trim() + " × " + sl.getText().trim()
                + " của " + t + "?", () -> thuHoi(t, id, so, khoa), k -> HopThoai.ketQua(cha, "Thu hồi ngọc", k));
    }

    static String thuHoi(final String charName, final String[] itemIds, final String[] quantities, final boolean isLockedGem) {
        try {
            if (CharManager.instance.getCharByCharName(charName) != null) {
                return "Người chơi đang online. Vui lòng chờ người chơi offline!";
            }
            final Connection conn = Database.instance.getConnection();
            final StringBuilder log = new StringBuilder("Thu hồi " + (isLockedGem ? "gem khóa" : "gem mở") + " từ " + charName + ":");
            try (PreparedStatement pre = conn.prepareStatement(
                    "SELECT listtemplate, soluong, slock FROM tob_gem_new WHERE owner IN (SELECT id FROM tob_char WHERE charname=?)")) {
                pre.setString(1, charName);
                try (ResultSet rs = pre.executeQuery()) {
                    if (!rs.next()) {
                        return "Không tìm thấy dữ liệu gem của Pokémon: " + charName;
                    }
                    final String[] templates = rs.getString("listtemplate").split(",");
                    final String[] amounts = (isLockedGem ? rs.getString("slock") : rs.getString("soluong")).split(",");
                    for (int i = 0; i < itemIds.length; i++) {
                        final int gemId = Integer.parseInt(itemIds[i].trim());
                        final int quantity = Integer.parseInt(quantities[i].trim());
                        for (int j = 0; j < templates.length && j < amounts.length; j++) {
                            if (Integer.parseInt(templates[j].trim()) == gemId) {
                                final int cur = Integer.parseInt(amounts[j].trim());
                                amounts[j] = String.valueOf(Math.max(0, cur - quantity));
                                log.append(" #").append(gemId).append(" -").append(quantity);
                                break;
                            }
                        }
                    }
                    try (PreparedStatement up = conn.prepareStatement("UPDATE tob_gem_new SET " + (isLockedGem ? "slock" : "soluong")
                            + "=? WHERE owner IN (SELECT id FROM tob_char WHERE charname=?)")) {
                        up.setString(1, String.join(",", amounts));
                        up.setString(2, charName);
                        up.executeUpdate();
                    }
                }
            } finally {
                Database.connPool.free(conn);
            }
            KiemToanQuanTri.ghi("THU_HOI_NGOC", charName, log.toString());
            return log.toString();
        } catch (final NumberFormatException e) {
            return "ID gem và số lượng phải là số!";
        } catch (final Exception e) {
            return "Lỗi khi thu hồi gem: " + e.getMessage();
        }
    }
}
