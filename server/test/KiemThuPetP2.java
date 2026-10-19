import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.htbb.pokemon.PetPokemon;
import com.htbb.pokemon.ThuPokemon;

/**
 * P2 (tai nguyen Pokemon): thu cuoi / pet / skin linh thu / hoa hinh / pet cu -> Pokemon.
 *
 *  - Bang SQL sql/pokemon_thu_pokemon.sql (data_thu_pokemon, sinh boi tools/pokemon/sinh_bang_thu.py) khop
 *    TUNG dong voi mang Java (ThuPokemon, PetPokemon) - SQL la ban ghi chinh thuc cua anh xa;
 *  - dong skin / hoa_hinh_* cua du_lieu/bang_thu.csv va du_lieu/bang_pet_cu.csv khop Java + client
 *    (jar AnimalMove.POKE_DEX_PET_CU, Unity AnimalMove.cs);
 *  - moi dex co sprite da dong goi o Unity (Resources/poke) va jar (res/pk);
 *  - migration idempotent (CREATE TABLE IF NOT EXISTS + REPLACE INTO, khong INSERT...SELECT);
 *  - client jar ve pet-tool (lop monster/Pet, pet co vat pham) bang Pokemon nhu Unity Pet.cs (truoc P2 jar
 *    ve anh hieu ung cu: cu, rong, doi, dai bang...), -93 uu tien hoa hinh -> skin -> loai thu.
 */
public final class KiemThuPetP2 {

    private KiemThuPetP2() {
    }

    public static void chay() {
        Kiem.nhom("P2 thu cuoi / pet / skin / hoa hinh -> Pokemon (SQL = Java = CSV = client)");
        Kiem.bang("6 skin linh thu co dex", Integer.valueOf(real.SkinThuCuoi.SO_SKIN), Integer.valueOf(ThuPokemon.soSkin()));
        for (int i = 0; i < ThuPokemon.soSkin(); i++) {
            Kiem.dung("skin " + i + " (" + real.SkinThuCuoi.TEN[i] + ") co dex", ThuPokemon.dexSkin(i) > 0);
        }
        Kiem.bang("skin ngoai bang -> 0", Integer.valueOf(0), Integer.valueOf(ThuPokemon.dexSkin(6)));
        Kiem.bang("hoa hinh pet cap 0 = cap 1", Integer.valueOf(PetPokemon.dexHoaHinh(1)), Integer.valueOf(PetPokemon.dexHoaHinh(0)));
        Kiem.bang("hoa hinh pet cap 9 = cap 5", Integer.valueOf(PetPokemon.dexHoaHinh(5)), Integer.valueOf(PetPokemon.dexHoaHinh(9)));
        Kiem.bang("21 pet cu", Integer.valueOf(21), Integer.valueOf(PetPokemon.soPetCu()));
        Kiem.bang("mau 23 Cu -> Hoothoot", "Hoothoot", PetPokemon.tenMauPet(23, 13));
        Kiem.bang("mau 0 (anh 0) -> Litwick", "Litwick", PetPokemon.tenMauPet(0, 0));

        try {
            kiemCsv();
            kiemSql();
            kiemClient();
        } catch (final Exception e) {
            Kiem.dung("P2 thu/pet: " + e, false);
        }
    }

    private static List<String> doc(final String duong) throws java.io.IOException {
        return Files.readAllLines(new File(duong).toPath(), StandardCharsets.UTF_8);
    }

    private static String docChu(final String duong) throws java.io.IOException {
        return new String(Files.readAllBytes(new File(duong).toPath()), StandardCharsets.UTF_8);
    }

    /** dex mong doi theo Java cho (loai, id). -1 = loai la. */
    private static int dexJava(final String loai, final int id) {
        if ("thu_cuoi".equals(loai)) {
            return ThuPokemon.dex(id);
        }
        if ("pet_item".equals(loai)) {
            return PetPokemon.dex(id);
        }
        if ("skin".equals(loai)) {
            return ThuPokemon.dexSkin(id);
        }
        if ("hoa_hinh_pet".equals(loai)) {
            return PetPokemon.dexHoaHinh(id);
        }
        if ("hoa_hinh_thu".equals(loai)) {
            return ThuPokemon.DEX_HOA_HINH;
        }
        if ("pet_cu".equals(loai)) {
            return PetPokemon.dexPetCu(id);
        }
        return -1;
    }

    private static boolean coSprite(final int dex, final boolean shiny) {
        final String k = String.format("%04d%s", Integer.valueOf(dex), shiny ? "s" : "");
        return new File("../client_pc_ios/Assets/Resources/poke/" + k + "_dl.bytes").isFile()
                && new File("../client_jar/res/pk/" + k + ".dl").isFile();
    }

    private static void kiemCsv() throws java.io.IOException {
        int skin = 0;
        int hh = 0;
        for (final String d : doc("../du_lieu/bang_thu.csv")) {
            final String[] c = d.split(",");
            if (c.length < 6 || !(c[0].equals("skin") || c[0].startsWith("hoa_hinh"))) {
                continue;
            }
            final int id = Integer.parseInt(c[1]);
            final int dex = Integer.parseInt(c[3]);
            Kiem.bang("bang_thu.csv " + c[0] + " " + id + " (" + c[5] + ") = Java", Integer.valueOf(dex), Integer.valueOf(dexJava(c[0], id)));
            Kiem.dung("co sprite jar + Unity: " + c[5] + " " + dex, coSprite(dex, "1".equals(c[4])));
            if ("skin".equals(c[0])) {
                skin++;
            } else {
                hh++;
            }
        }
        Kiem.bang("bang_thu.csv du 6 dong skin", Integer.valueOf(ThuPokemon.soSkin()), Integer.valueOf(skin));
        Kiem.bang("bang_thu.csv du 6 dong hoa hinh (5 pet + 1 thu)", Integer.valueOf(6), Integer.valueOf(hh));
        int n = 0;
        for (final String d : doc("../du_lieu/bang_pet_cu.csv").subList(1, doc("../du_lieu/bang_pet_cu.csv").size())) {
            final String[] c = d.split(",");
            final int idImg = Integer.parseInt(c[0]);
            final int dex = Integer.parseInt(c[2]);
            Kiem.bang("bang_pet_cu.csv " + idImg + " dex = Java", Integer.valueOf(dex), Integer.valueOf(PetPokemon.dexPetCu(idImg)));
            Kiem.bang("bang_pet_cu.csv " + idImg + " ten = Java", c[4], PetPokemon.tenPetCu(idImg));
            Kiem.dung("co sprite pet cu " + dex, coSprite(dex, false));
            n++;
        }
        Kiem.bang("bang_pet_cu.csv = PetPokemon.PET_CU", Integer.valueOf(PetPokemon.soPetCu()), Integer.valueOf(n));
    }

    private static void kiemSql() throws java.io.IOException {
        final String sql = docChu("../sql/pokemon_thu_pokemon.sql");
        Kiem.dung("CREATE TABLE IF NOT EXISTS data_thu_pokemon", sql.contains("CREATE TABLE IF NOT EXISTS `data_thu_pokemon`"));
        Kiem.sai("khong INSERT ... SELECT", Pattern.compile("(?is)INSERT\\s+INTO[^;]*SELECT").matcher(sql).find());
        Kiem.sai("khong ON DUPLICATE", sql.toUpperCase().contains("ON DUPLICATE"));
        final Matcher so = Pattern.compile("SO_DONG: (\\d+)").matcher(sql);
        Kiem.dung("co dong SO_DONG", so.find());
        final Matcher m = Pattern.compile("VALUES \\('([a-z_]+)', (\\d+), (\\d+), (\\d), '((?:[^']|'')*)'\\);").matcher(sql);
        int dem = 0;
        int lech = 0;
        int thieuSprite = 0;
        while (m.find()) {
            dem++;
            final String loai = m.group(1);
            final int id = Integer.parseInt(m.group(2));
            final int dex = Integer.parseInt(m.group(3));
            if (dexJava(loai, id) != dex) {
                lech++;
                Kiem.bang("SQL " + loai + " " + id + " = Java", Integer.valueOf(dexJava(loai, id)), Integer.valueOf(dex));
            }
            if (!coSprite(dex, "1".equals(m.group(4)))) {
                thieuSprite++;
                Kiem.dung("SQL " + loai + " " + id + " dex " + dex + " co sprite", false);
            }
        }
        Kiem.bang("SQL so dong = SO_DONG", Integer.valueOf(so.find(0) ? Integer.parseInt(so.group(1)) : -1), Integer.valueOf(dem));
        Kiem.bang("SQL du dong (21 thu + pet_item + 21 pet cu + 6 skin + 6 hoa hinh)",
                Integer.valueOf(ThuPokemon.soLoai() + PetPokemon.soDong() + PetPokemon.soPetCu() + ThuPokemon.soSkin() + 6), Integer.valueOf(dem));
        Kiem.bang("SQL khop Java tung dong", Integer.valueOf(0), Integer.valueOf(lech));
        Kiem.bang("moi dex trong SQL co sprite", Integer.valueOf(0), Integer.valueOf(thieuSprite));
    }

    private static void kiemClient() throws java.io.IOException {
        final String jar = docChu("../client_jar/src/game/model/AnimalMove.java");
        final String unity = docChu("../client_pc_ios/Assets/Scripts/Assembly-CSharp/AnimalMove.cs");
        for (int idImg = 0; idImg <= 20; idImg++) {
            final int dex = PetPokemon.dexPetCu(idImg);
            Kiem.dung("jar POKE_DEX_PET_CU {" + idImg + ", " + dex + "}", jar.contains("{" + idImg + ", " + dex + "}"));
            Kiem.dung("Unity POKE_DEX_PET_CU { " + idImg + ", " + dex + " }", unity.contains("{ " + idImg + ", " + dex + " }"));
        }
        final String pet = docChu("../client_jar/src/game/model/monster/Pet.java");
        final int ve = pet.indexOf("public void paint(Graphics g)");
        Kiem.dung("jar Pet.paint ve Pokemon (vePokePet) truoc anh hieu ung cu",
                ve >= 0 && pet.indexOf("vePokePet(g, chu)", ve) > ve && pet.indexOf("vePokePet(g, chu)", ve) < pet.indexOf("ALL_PET_DATA.get", ve));
        Kiem.dung("jar Pet.dexPetCu = 0 (pet-tool khong tra bang pet cu)", pet.contains("protected int dexPetCu() {\n        return 0;"));
        final String th = docChu("src/real/TienHoaPokemon.java");
        final int a = th.indexOf("public static int[] dexCuoi");
        Kiem.dung("-93: hoa hinh truoc skin truoc loai thu", a > 0
                && th.indexOf("DEX_HOA_HINH", a) < th.indexOf("dexSkin", a) && th.indexOf("dexSkin", a) < th.indexOf("ThuPokemon.dex(a.idImg)", a));
        Kiem.dung("taoPokeCuoi dung dexCuoi", th.contains("final int[] ds = dexCuoi(ch);"));
        Kiem.dung("-94 hoa hinh pet -> Pokemon (khong con gui 0)", th.contains("PetPokemon.dexHoaHinh(pet.capHoaHinh)"));
        Kiem.dung("ChuPokemon.ap doi ten mau pet cu", docChu("src/com/htbb/pokemon/ChuPokemon.java").contains("apTenPet();"));
    }
}
