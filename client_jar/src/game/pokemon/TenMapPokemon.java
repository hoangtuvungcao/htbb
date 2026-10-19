package game.pokemon;

/**
 * Doi ten dia danh game goc sang dia danh Pokemon - ban J2ME cua Unity TenMapPokemon.cs
 * (ban goc: HTBB src/com/htbb/pokemon/TenMapPokemon.java). Dung cho ten viet cung o client
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

    /**
     * Vung Hoenn (nam 2, ban do moi 43..62 / 160..165 / 280..282 - com.htbb.pokemon.BanDoHoenn): ten
     * theo id nap. BAN SAO o client J2ME (game/pokemon/TenMapPokemon) va Unity (TenMapPokemon.cs) -
     * test/KiemThuPokemon + client_jar/tools/KiemPokeDuLieu doi chieu.
     */
    public static final int[] HOENN_ID = {43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 160, 161, 162, 163, 164, 165, 280, 281, 282};
    public static final String[] HOENN_TEN = {"Littleroot", "Rừng Petalburg", "Rustboro", "Bờ biển Dewford", "Slateport", "Route 110", "Mauville", "Sa mạc Route 111", "Lavaridge", "Núi Chimney", "Rừng mưa Route 119", "Fortree", "Route 120 - Viện Thời tiết", "Lilycove", "Núi Pyre", "Biển Route 124", "Mossdeep", "Sootopolis", "Biển Route 131", "Ever Grande", "Hang Granite", "Thác Meteor", "Hang dưới biển", "Hang Khởi Nguyên", "Tháp Trời", "Đường Victory Hoenn", "Đỉnh Tháp Trời", "Liên minh Hoenn", "Sảnh Nhà vô địch"};

    /** Ten ban do Hoenn theo id nap, "" neu khong phai Hoenn. */
    public static String tenHoenn(int id) {
        for (int i = 0; i < HOENN_ID.length; i++) {
            if (HOENN_ID[i] == id) {
                return HOENN_TEN[i];
            }
        }
        return "";
    }

    private static final String[] HUONG = {"Đông ", "Tây ", "Nam ", "Bắc "};

    /**
     * Chỉ client (KHÔNG có trong bảng server - KiemPokeDuLieu chỉ đối chiếu BANG): tên riêng kiếm hiệp còn
     * sót trong Map.namMap của server / tên ghép số khu. Dùng qua doiHien() khi HIỆN tên bản đồ.
     * Tên chung (Bến tàu, Khu mua bán, Hang phía đông, Thuyền...) giữ nguyên. Tên thiết kế server gửi từ bảng SQL
     * data_map_ten (Ngục tối, Khu mỏ, Chiến trường, Đấu trường La Mã) KHÔNG đổi ở đây - client hiện đúng tên server.
     */
    public static final String[][] BANG_HIEN = {
        {"Hang Dracula phía đông", "Hang Darkrai phía đông"},
        {"Hang Dracula phía tây", "Hang Darkrai phía tây"},
        {"Hang Dracula phía nam", "Hang Darkrai phía nam"},
        {"Hang Dracula phía bắc", "Hang Darkrai phía bắc"},
        {"Hang thằn lằn đông", "Hang Salamence đông"},
        {"Hang thằn lằn tây", "Hang Salamence tây"},
        {"Hang thằn lằn nam", "Hang Salamence nam"},
        {"Hang thằn lằn bắc", "Hang Salamence bắc"},
        {"Lãnh địa bang", "Lãnh địa đội"},
        {"TYT 5x", "Tháp luyện 5x"},
        {"TYT 6x", "Tháp luyện 6x"},
        {"TYT 7x", "Tháp luyện 7x"},
        {"Chien truong", "Chiến trường"},
        {"Lôi đài", "Sàn đấu"},
        {"Cân núi", "Đỉnh núi"},
        {"Làng Sơn Nam", "Pewter"},
        {"Thành liên đấu", "Gym liên đấu"},
    };

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

    private static String traHien(String ten) {
        for (int i = 0; i < BANG_HIEN.length; i++) {
            if (bang(BANG_HIEN[i][0], ten)) {
                return BANG_HIEN[i][1];
            }
        }
        return null;
    }

    /**
     * Tên bản đồ để HIỆN (thanh tên vùng, bản đồ lớn): doi() + bảng chỉ-client + tên có số khu ở cuối
     * ("Bạch hạc 1" -> "Goldenrod 1", "Thành liên đấu 3" -> "Gym liên đấu 3", khớp từ điển server).
     */
    public static String doiHien(String ten) {
        if (ten == null || ten.length() == 0) {
            return ten;
        }
        String t = ten.trim();
        String r = doi(t);
        if (!r.equals(t)) {
            return r;
        }
        String h = traHien(t);
        if (h != null) {
            return h;
        }
        int i = t.length();
        while (i > 0 && t.charAt(i - 1) >= '0' && t.charAt(i - 1) <= '9') {
            i--;
        }
        char tach = i > 1 && i < t.length() ? t.charAt(i - 1) : 'x';
        if (tach == ' ' || tach == '-' || tach == '_') {
            // server gửi "Bạch hạc_1" (số khu): đổi tên gốc, hiện "Goldenrod 1"
            String goc = t.substring(0, i - 1).trim();
            String d = doiHien(goc);
            if (!d.equals(goc) || tach == '_') {
                // server (bảng data_map_ten) gửi sẵn tên Pokémon "Goldenrod_1": vẫn hiện "Goldenrod 1"
                return d + " " + t.substring(i);
            }
        }
        return ten;
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
