package com.htbb.pokemon;

/**
 * W2: doi ten dia danh goc sang dia danh the gioi Pokemon (Kanto/Johto).
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

    /**
     * Vung Hoenn (nam 2, ban do moi 43..62 / 160..165 / 280..282 - com.htbb.pokemon.BanDoHoenn): ten
     * theo id nap. BAN SAO o client J2ME (game/pokemon/TenMapPokemon) va Unity (TenMapPokemon.cs) -
     * test/KiemThuPokemon + j2me/tools/KiemPokeDuLieu doi chieu.
     */
    public static final int[] HOENN_ID = {43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 160, 161, 162, 163, 164, 165, 280, 281, 282};
    public static final String[] HOENN_TEN = {"Littleroot", "Rừng Petalburg", "Rustboro", "Bờ biển Dewford", "Slateport", "Route 110", "Mauville", "Sa mạc Route 111", "Lavaridge", "Núi Chimney", "Rừng mưa Route 119", "Fortree", "Route 120 - Viện Thời tiết", "Lilycove", "Núi Pyre", "Biển Route 124", "Mossdeep", "Sootopolis", "Biển Route 131", "Ever Grande", "Hang Granite", "Thác Meteor", "Hang dưới biển", "Hang Khởi Nguyên", "Tháp Trời", "Đường Victory Hoenn", "Đỉnh Tháp Trời", "Liên minh Hoenn", "Sảnh Nhà vô địch"};

    /** Ten ban do Hoenn theo id nap, "" neu khong phai Hoenn. */
    public static String tenHoenn(final int id) {
        for (int i = 0; i < HOENN_ID.length; i++) {
            if (HOENN_ID[i] == id) {
                return HOENN_TEN[i];
            }
        }
        return "";
    }

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
    /**
     * Nho ket qua (ham thuan, tap ten dia danh huu han): goi moi lan doi map cho MOI dong trong danh
     * sach dia diem (createMessageLocation) - truoc day ~3 % CPU o 300 nguoi (equalsIgnoreCase x 38
     * dong x 5 tien to). Tran 4096 muc de khong phinh neu co ai dua ten la vao.
     */
    private static final java.util.concurrent.ConcurrentHashMap<String, String> DA_DOI = new java.util.concurrent.ConcurrentHashMap<String, String>();

    public static String doi(final String ten) {
        if (ten == null || ten.isEmpty()) {
            return ten;
        }
        final String daCo = DA_DOI.get(ten);
        if (daCo != null) {
            return daCo;
        }
        final String kq = doiThat(ten);
        if (DA_DOI.size() < 4096) {
            DA_DOI.put(ten, kq);
        }
        return kq;
    }

    private static String doiThat(final String ten) {
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
