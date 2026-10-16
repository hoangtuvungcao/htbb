package com.kpah.pokemon;

/**
 * W2: doi ten dia danh KPAH sang dia danh the gioi Pokemon (Kanto/Johto).
 *
 * Ap dung khi gui ten (MessageCreator.createMessageLocation, lenh 105) neu che do Pokemon
 * bat. Giu tien to huong "Đông/Tây/Nam/Bắc " ("Đông Dao Châu" -> "Đông Pallet"); ten chung
 * (Bến tàu, Khu mua bán, Hang phía đông...) giu nguyen. Client co ban sao bang nay o
 * ClientUnity/Assets/Scripts/Game/TenMapPokemon.cs (ten viet cung trong Tilemap.cs) -
 * test/KiemThuPokemon doi chieu hai bang phai giong het.
 */
public final class TenMapPokemon {

    /** {ten cu (so sanh khong phan biet hoa thuong), ten moi}. */
    public static final String[][] BANG = {
        {"Dao Châu", "Pallet"},
        {"Tiên Du", "Viridian"},
        {"Sơn Nam", "Pewter"},
        {"Kỳ Bố", "Cerulean"},
        {"Phù Liệt", "Vermilion"},
        {"Hàm Tử", "Lavender"},
        {"Thạch Giang", "Celadon"},
        {"Đông Sơn", "Saffron"},
        {"Tử Quan", "Fuchsia"},
        {"Trường Giang", "Cinnabar"},
        {"Sơn Lâm", "Rừng Viridian"},
        {"Phong Châu", "Núi Mặt Trăng"},
        {"Bình Lục", "Vùng Safari"},
        {"Lộc Trĩ", "New Bark"},
        {"Bảo Thái", "Cherrygrove"},
        {"Hầm Tối", "Hang Tối"},
        {"Hồi Hồ", "Hồ Phẫn Nộ"},
        {"Siêu Loại", "Violet"},
        {"Cẩm Khê", "Azalea"},
        {"Bạch Hạc", "Goldenrod"},
        {"Tế Giang", "Ecruteak"},
        {"Đỗ Đông", "Olivine"},
        {"Thành Trấn Danh", "Cao nguyên Indigo"},
        {"Biên Giới", "Đường Victory"},
        {"Hang Thằn Lằn", "Hang Salamence"},
        {"Hang Đại Bàng", "Tổ Zapdos"},
        {"Hang Dracula", "Hang Darkrai"},
        {"Hang Mãng Xà", "Hang Serperior"},
        {"Cánh Rừng Đông", "Rừng Ilex"},
        {"Rừng Già", "Rừng Cổ"},
        {"Bình Kiều", "Mahogany"},
        {"Dương Đông", "Blackthorn"},
        {"Lâm Tây", "Cianwood"},
        {"Chiến Hạm", "Tàu S.S. Anne"},
        {"Núi kho báu", "Núi Silver"},
        {"Nhà bán HP", "Trung tâm Pokémon"},
        {"Nhà bán MP", "Poké Mart"},
        {"Nhà thợ rèn", "Xưởng chế tạo"},
    };

    private static final String[] HUONG = {"Đông ", "Tây ", "Nam ", "Bắc "};

    private TenMapPokemon() {
    }

    private static String traBang(final String ten) {
        for (final String[] d : BANG) {
            if (d[0].equalsIgnoreCase(ten)) {
                return d[1];
            }
        }
        return null;
    }

    /** Ten moi cho {@code ten}; khong co trong bang thi tra lai nguyen ven. */
    public static String doi(final String ten) {
        if (ten == null || ten.isEmpty()) {
            return ten;
        }
        final String nguyen = traBang(ten.trim());
        if (nguyen != null) {
            return nguyen;
        }
        for (final String h : HUONG) {
            if (ten.regionMatches(true, 0, h, 0, h.length())) {
                final String sau = traBang(ten.substring(h.length()).trim());
                if (sau != null) {
                    return h + sau;
                }
            }
        }
        return ten;
    }
}
