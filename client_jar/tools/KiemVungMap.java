import game.pokemon.VungMap;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Nhãn VÙNG trên bản đồ nhỏ đúng vùng địa lý của từng map:
 *  1. game.pokemon.VungMap.BANG (client) == du_lieu/vung_map.csv;
 *  2. mọi map trong du_lieu/thiet_ke_map.csv có tên địa danh của một vùng (Pewter, Goldenrod, Littleroot...)
 *     mang đúng nhãn vùng đó; map có nhãn phải có trong thiet_ke_map.csv; map chung (đấu trường...) không nhãn.
 *
 *   javac -cp <lop jar> -d /tmp/kv client_jar/tools/KiemVungMap.java && java -cp <lop jar>:/tmp/kv KiemVungMap du_lieu
 */
public class KiemVungMap {
    static final String[] KANTO = {"Pewter", "Pallet", "Viridian", "Vermilion", "Cerulean", "Lavender", "Celadon",
        "Saffron", "Fuchsia", "Cinnabar", "Nui Mat Trang", "Vung Safari", "S.S. Anne", "Indigo"};
    static final String[] JOHTO = {"New Bark", "Cherrygrove", "Goldenrod", "Ecruteak", "Violet", "Azalea", "Olivine",
        "Mahogany", "Ilex", "Blackthorn", "Cianwood", "Hang Toi", "Ho Phan No"};
    static final String[] HOENN = {"Littleroot", "Petalburg", "Rustboro", "Dewford", "Slateport", "Route 1", "Mauville",
        "Lavaridge", "Chimney", "Fortree", "Lilycove", "Pyre", "Mossdeep", "Sootopolis", "Ever Grande", "Granite",
        "Meteor", "Hang duoi bien", "Khoi Nguyen", "Thap Troi", "Victory Hoenn", "Lien minh Hoenn", "Nha vo dich"};

    static int loi;

    static void sai(String s) {
        loi++;
        System.out.println("SAI: " + s);
    }

    public static void main(String[] a) throws Exception {
        File dl = new File(a.length > 0 ? a[0] : "du_lieu");
        Map<Integer, String> csv = new HashMap<Integer, String>();
        for (String d : Files.readAllLines(new File(dl, "vung_map.csv").toPath(), StandardCharsets.UTF_8)) {
            if (d.startsWith("#") || d.startsWith("id;") || d.trim().isEmpty()) continue;
            String[] p = d.split(";");
            csv.put(Integer.valueOf(p[0].trim()), p[1].trim());
        }
        // 1. bảng client == csv
        if (VungMap.BANG.length != csv.size()) sai("VungMap.BANG " + VungMap.BANG.length + " dong, vung_map.csv " + csv.size());
        int truoc = -1;
        for (short[] r : VungMap.BANG) {
            if (r[0] <= truoc) sai("VungMap.BANG khong tang dan tai " + r[0]);
            truoc = r[0];
            String c = csv.get((int) r[0]);
            if (c == null || !c.equals(VungMap.TEN[r[1]])) sai("map " + r[0] + ": client " + VungMap.TEN[r[1]] + ", csv " + c);
            if (!VungMap.TEN[r[1]].equals(VungMap.ten(r[0]))) sai("VungMap.ten(" + r[0] + ") tra sai");
        }
        // 2. đối chiếu tên địa danh trong thiet_ke_map.csv
        List<String> tk = Files.readAllLines(new File(dl, "thiet_ke_map.csv").toPath(), StandardCharsets.UTF_8);
        Map<Integer, String> ten = new HashMap<Integer, String>();
        for (String d : tk) {
            if (d.startsWith("#") || d.startsWith("id;") || d.trim().isEmpty()) continue;
            String[] p = d.split(";");
            ten.put(Integer.valueOf(p[0].trim()), p[2].trim());
        }
        int soKiem = 0;
        for (Map.Entry<Integer, String> e : ten.entrySet()) {
            String mong = vungTheoTen(e.getValue());
            String co = VungMap.ten(e.getKey());
            if (mong != null) {
                soKiem++;
                if (!mong.equals(co)) sai("map " + e.getKey() + " '" + e.getValue() + "': nhan " + co + ", dia danh thuoc " + mong);
            }
        }
        for (Integer id : csv.keySet()) {
            if (!ten.containsKey(id)) sai("map " + id + " co nhan vung nhung khong co trong thiet_ke_map.csv");
        }
        for (int id : new int[]{105, 202, 203, 25, 208}) {
            if (VungMap.ten(id) != null) sai("map chung " + id + " khong duoc co nhan vung");
        }
        System.out.println("KiemVungMap: " + VungMap.BANG.length + " map co nhan, " + soKiem + " map doi chieu dia danh, " + loi + " loi");
        if (loi > 0) System.exit(1);
    }

    static String vungTheoTen(String t) {
        if (chua(t, HOENN)) return "Hoenn";
        if (chua(t, JOHTO)) return "Johto";
        if (chua(t, KANTO)) return "Kanto";
        return null;
    }

    static boolean chua(String t, String[] ds) {
        for (String s : ds) {
            if (t.contains(s)) return true;
        }
        return false;
    }
}
