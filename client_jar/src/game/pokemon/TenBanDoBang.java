package game.pokemon;

/**
 * TÊN BẢN ĐỒ người chơi thấy - SINH TỰ ĐỘNG từ SQL data_map_ten (tools/pokemon/sinh_ten_ban_do_client.py), KHÔNG SỬA
 * TAY. Tilemap.setNameMap / bản đồ vùng / xa phu dùng bảng này thay tên viết cứng cũ.
 */
public final class TenBanDoBang {
    static final short[] ID = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 70, 72, 73, 74, 78, 79, 80, 82, 83, 84, 88, 89, 90, 91, 92, 93, 94, 95, 96, 97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 107, 108, 109, 110, 111, 112, 113, 114, 115, 116, 117, 118, 122, 160, 161, 162, 163, 164, 165, 201, 202, 203, 204, 205, 206, 207, 208, 209, 210, 225, 226, 227, 228, 260, 261, 262, 263, 264, 265, 266, 267, 268, 269, 270, 271, 280, 281, 282, 2541};
    static final String[] TEN = {
        "Pewter", "Pallet", "Viridian", "Vermilion", "Cerulean", "Lavender",
        "Celadon", "Saffron", "Fuchsia", "Cinnabar", "New Bark", "Rừng Viridian",
        "Khu mua bán", "Núi Mặt Trăng", "Vùng Safari", "Cherrygrove", "Khu mỏ", "Goldenrod",
        "Ecruteak", "Violet", "Đồng cỏ phía tây", "Tàu S.S. Anne", "Azalea", "Olivine",
        "Tháp luyện tập", "Rừng Cổ", "Chiến trường", "Mahogany", "Rừng Ilex", "Sân Liên minh 1",
        "Sân Liên minh 2", "Sân Liên minh 3", "Sân Liên minh 4", "Sân Liên minh 5", "Sân Liên minh 6", "Sân Liên minh 7",
        "Sân Liên minh 8", "Sân Liên minh 9", "Sân Liên minh 10", "Chiến trường", "Chân núi Silver", "Núi Silver",
        "Littleroot", "Rừng Petalburg", "Rustboro", "Bờ biển Dewford", "Slateport", "Route 110",
        "Mauville", "Sa mạc Route 111", "Lavaridge", "Núi Chimney", "Rừng mưa Route 119", "Fortree",
        "Route 120 - Viện Thời tiết", "Lilycove", "Núi Pyre", "Biển Route 124", "Mossdeep", "Sootopolis",
        "Biển Route 131", "Ever Grande", "Blackthorn", "Nhà (Blackthorn)", "Trung tâm Pokémon", "Poké Mart",
        "Nhà kho", "Xưởng chế tạo", "Cianwood", "Nhà (Cianwood)", "Trung tâm Pokémon", "Poké Mart",
        "Nhà kho", "Xưởng chế tạo", "Cửa hàng 1", "Cửa hàng 2", "Cửa hàng 3", "Cửa hàng 4",
        "Cửa hàng 5", "Cửa hàng 6", "Cửa hàng 7", "Cửa hàng 8", "Cửa hàng 9", "Cửa hàng 10",
        "Nhà (Pewter)", "Khoảng rừng nhỏ", "Nhà (Pewter) 2", "Trung tâm Pokémon", "Poké Mart", "Sân nhà khởi đầu",
        "Bến tàu", "Thuyền", "Nhà kho", "Xưởng chế tạo", "Hang động", "Hang Darkrai",
        "Hang Salamence", "Hang Tối", "Hồ Phẫn Nộ", "Hang nhỏ", "Hang cổ", "Ngục tối",
        "Đường Victory", "Hang trống", "Hang Granite", "Thác Meteor", "Hang dưới biển", "Hang Khởi Nguyên",
        "Tháp Trời", "Đường Victory Hoenn", "Cao nguyên Indigo", "Đấu trường", "Đấu trường La Mã", "Đấu trường 2",
        "Đấu trường 3", "Lãnh địa đội", "Lãnh địa đội 2", "Tháp luyện tập 5x", "Tháp luyện tập 6x", "Tháp luyện tập 7x",
        "Lễ cưới 1", "Lễ cưới 2", "Sàn đấu", "Sảnh chờ", "Nhà (Indigo) 1", "Nhà (Indigo) 2",
        "Nhà (Indigo) 3", "Nhà (Indigo) 4", "Nhà (Indigo) 5", "Nhà (Indigo) 6", "Nhà (Indigo) 7", "Nhà (Indigo) 8",
        "Nhà (Indigo) 9", "Nhà (Indigo) 10", "Nhà (Indigo) 11", "Nhà (Indigo) 12", "Đỉnh Tháp Trời", "Liên minh Hoenn",
        "Sảnh Nhà vô địch", "Tổ Zapdos"
    };

    private TenBanDoBang() {
    }

    /** Tên bản đồ id (SQL); null = không có trong bảng. */
    public static String ten(int id) {
        int lo = 0, hi = ID.length - 1;
        while (lo <= hi) {
            int m = (lo + hi) >>> 1;
            if (ID[m] < id) {
                lo = m + 1;
            } else if (ID[m] > id) {
                hi = m - 1;
            } else {
                return TEN[m];
            }
        }
        return null;
    }

    /** Tên bản đồ id, không có thì tên dự phòng (đã đổi qua TenMapPokemon). */
    public static String ten(int id, String duPhong) {
        String t = ten(id);
        return t != null && t.length() > 0 ? t : TenMapPokemon.doiHien(duPhong);
    }
}
