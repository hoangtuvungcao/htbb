package game.pokemon;

/**
 * Doi ten dia danh KPAH sang dia danh Pokemon - ban J2ME cua ClientUnity TenMapPokemon.cs
 * (ban goc: KPAH src/com/kpah/pokemon/TenMapPokemon.java). Dung cho ten viet cung o client
 * (Tilemap); ten server gui (lenh 105) da duoc server doi san.
 */
public final class TenMapPokemon {
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

    private static boolean bang(String a, String b) {
        return a.length() == b.length() && ChuPokemon.thuong(a).equals(ChuPokemon.thuong(b));
    }

    private static String traBang(String ten) {
        for (int i = 0; i < BANG.length; i++) {
            if (bang(BANG[i][0], ten)) {
                return BANG[i][1];
            }
        }
        return null;
    }

    public static String doi(String ten) {
        if (ten == null || ten.length() == 0) {
            return ten;
        }
        String nguyen = traBang(ten.trim());
        if (nguyen != null) {
            return nguyen;
        }
        for (int i = 0; i < HUONG.length; i++) {
            String h = HUONG[i];
            if (ten.length() > h.length() && bang(ten.substring(0, h.length()), h)) {
                String sau = traBang(ten.substring(h.length()).trim());
                if (sau != null) {
                    return h + sau;
                }
            }
        }
        return ten;
    }
}
