import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import real.Map;
import real.MonsterTemplate;
import real.NoiDungLauDai;

/**
 * Quai nam 2 (Hoenn, template 139..) thay 1/3 quai cap >= 84 luc hoi sinh (NoiDungLauDai.khiHoiSinh). Cong kiem bot
 * 31/10/2026: lay ca dai (cap+3) -> Mahogany (Luxray 84) moc Wailord / Zangoose / Grumpig / Banette 91-92; quai cao hon
 * nguoi choi 6+ cap x2 tan cong, nua thu (LiveActor.attackDam): don 5994 vao nhan vat cap 85 maxhp 21851. Nay chi loai lech
 * cap goc <= 2. Du lieu: bang data_noi_dung_quai_dai_cap that trong sql/htbb.sql.
 */
public final class KiemQuaiNam2 {

    private KiemQuaiNam2() {
    }

    public static void chay() {
        Kiem.nhom("Quai nam 2 thay quai goc chi lech cap <= 2 (Mahogany cap 84 khong moc quai cap 91-92)");
        final Hashtable<Integer, MonsterTemplate> cu = Map.monsterTemplates;
        try {
            final String sql = new String(Files.readAllBytes(Paths.get("../sql/htbb.sql")), StandardCharsets.UTF_8);
            final int i = sql.indexOf("INSERT INTO `data_noi_dung_quai_dai_cap`");
            Kiem.dung("sql co data_noi_dung_quai_dai_cap", i >= 0);
            if (i < 0) {
                return;
            }
            final String dong = sql.substring(i, sql.indexOf(";\n", i));
            // moi dai: (cap_tu, ..., 'id:Ten(cap)|...') - lay cap_tu dau bo va moi id:Ten(cap)
            final HashMap<Integer, int[]> bang = new HashMap<Integer, int[]>();
            final Hashtable<Integer, MonsterTemplate> mau = new Hashtable<Integer, MonsterTemplate>();
            // moi dong: '86-90',86,90,loai_co_san,...,template_nam2,so_template_nam2 - template_nam2 = 'id:Ten(cap)|...' cuoi dong
            for (final String d : dong.substring(dong.indexOf("('") + 2).split("\\),\\('")) {
                final Matcher bo = Pattern.compile("^\\d+-\\d+',(\\d+),.*'((?:\\d+:[^|'(),]+\\(\\d+\\)\\|?)+)',\\d+\\)?$").matcher(d);
                if (!bo.find()) {
                    continue;
                }
                final Matcher t = Pattern.compile("(\\d+):[^|'()]+\\((\\d+)\\)").matcher(bo.group(2));
                final java.util.List<Integer> ds = new java.util.ArrayList<Integer>();
                while (t.find()) {
                    final int id = Integer.parseInt(t.group(1));
                    ds.add(Integer.valueOf(id));
                    final MonsterTemplate m = new MonsterTemplate();
                    m.id = id;
                    m.level = (byte) Integer.parseInt(t.group(2));
                    mau.put(Integer.valueOf(id), m);
                }
                final int[] a = new int[ds.size()];
                for (int k = 0; k < a.length; k++) {
                    a[k] = ds.get(k).intValue();
                }
                bang.put(Integer.valueOf(Integer.parseInt(bo.group(1))), a);
            }
            Kiem.dung("doc duoc cac dai nam 2 (>= 4 dai, co dai 86)", bang.size() >= 4 && bang.containsKey(Integer.valueOf(86)));
            Map.monsterTemplates = mau;
            boolean lech = false, rong = false;
            final StringBuilder sai = new StringBuilder();
            for (int cap = 84; cap <= 98; cap++) {
                final int[] ds = NoiDungLauDai.chonNam2(cap, bang);
                if (ds.length == 0) {
                    rong = true;
                    sai.append(" cap ").append(cap).append(" rong;");
                }
                for (final int id : ds) {
                    final int c = mau.get(Integer.valueOf(id)).level;
                    if (Math.abs(c - cap) > NoiDungLauDai.LECH_CAP_NAM2) {
                        lech = true;
                        sai.append(" cap ").append(cap).append(" -> ").append(id).append("(").append(c).append(");");
                    }
                }
            }
            Kiem.dung("moi quai nam 2 lech cap goc <= " + NoiDungLauDai.LECH_CAP_NAM2 + sai, !lech);
            Kiem.dung("cap 84..98 deu con loai nam 2 de thay (noi dung nam 2 van xuat hien)" + sai, !rong);
            // Luxray 84 (Mahogany): khong con Wailord 158 (91), Zangoose 159 (91), Grumpig 160 (92), Banette 161 (92)
            final int[] lux = NoiDungLauDai.chonNam2(84, bang);
            boolean coCao = false;
            for (final int id : lux) {
                coCao |= id >= 158 && id <= 161;
            }
            Kiem.dung("Luxray 84 khong thay bang Wailord / Zangoose / Grumpig / Banette (91-92)", !coCao && lux.length > 0);
            Kiem.dung("bang rong / null -> khong thay", NoiDungLauDai.chonNam2(84, null).length == 0
                    && NoiDungLauDai.chonNam2(84, new HashMap<Integer, int[]>()).length == 0);
        } catch (final Exception e) {
            e.printStackTrace();
            Kiem.dung("khong nem loi: " + e, false);
        } finally {
            Map.monsterTemplates = cu;
        }
    }
}
