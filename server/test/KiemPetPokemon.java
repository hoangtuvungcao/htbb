import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

import com.htbb.pokemon.GoiPokemon;
import com.htbb.pokemon.PetPokemon;

/**
 * Kiem thu pet di theo Pokemon (com.htbb.pokemon.PetPokemon) + goi -94 POKE_PET.
 *
 * Bang PetPokemon.BANG duoc chep tay tu cac dong loai=pet_item cua du_lieu/bang_thu.csv -
 * test doi chieu TUNG dong CSV voi bang Java (va nguoc lai qua so dong). Kem kiem client co
 * case -94 va ve pet bang PokeBody.
 */
public final class KiemPetPokemon {

    public static void chay() {
        kiemChuoiGoi();
        Kiem.nhom("com.htbb.pokemon.PetPokemon - khop bang_thu.csv (pet_item)");
        Kiem.bang("41 item pet", Integer.valueOf(41), Integer.valueOf(PetPokemon.soDong()));
        Kiem.bang("item ngoai bang -> 0", Integer.valueOf(0), Integer.valueOf(PetPokemon.dex(714)));
        Kiem.bang("id 0 -> 0", Integer.valueOf(0), Integer.valueOf(PetPokemon.dex(0)));
        Kiem.sai("item ngoai bang khong shiny", PetPokemon.shiny(714));
        Kiem.bang("Zubat cap 2 (705)", Integer.valueOf(41), Integer.valueOf(PetPokemon.dex(705)));
        Kiem.bang("Golbat cap 3 (706)", Integer.valueOf(42), Integer.valueOf(PetPokemon.dex(706)));
        Kiem.bang("Crobat cap 5 (708)", Integer.valueOf(169), Integer.valueOf(PetPokemon.dex(708)));
        try {
            final List<String> dong = Files.readAllLines(new File("../du_lieu/bang_thu.csv").toPath(), StandardCharsets.UTF_8);
            int dem = 0;
            for (final String d : dong) {
                final String[] c = d.split(",");
                if (c.length < 6 || !"pet_item".equals(c[0])) {
                    continue;
                }
                dem++;
                final int id = Integer.parseInt(c[1]);
                final int dex = Integer.parseInt(c[3]);
                final boolean shiny = "1".equals(c[4]);
                Kiem.bang("item " + id + " (" + c[5] + ") dex", Integer.valueOf(dex), Integer.valueOf(PetPokemon.dex(id)));
                Kiem.bang("item " + id + " shiny", Boolean.valueOf(shiny), Boolean.valueOf(PetPokemon.shiny(id)));
                Kiem.dung("co sprite da dong goi: " + dex + (shiny ? "s" : ""), new File(String.format(
                        "../client_pc_ios/Assets/Resources/poke/%04d%s_dl.bytes", dex, shiny ? "s" : "")).isFile());
            }
            Kiem.bang("CSV va bang Java cung so dong pet_item", Integer.valueOf(PetPokemon.soDong()), Integer.valueOf(dem));
        } catch (final Exception e) {
            Kiem.dung("doc bang_thu.csv: " + e, false);
        }

        Kiem.nhom("com.htbb.pokemon.GoiPokemon - goi -94 POKE_PET");
        Kiem.bang("so lenh -94", Integer.valueOf(-94), Integer.valueOf(GoiPokemon.POKE_PET));
        try {
            final ByteArrayOutputStream bo = new ByteArrayOutputStream();
            GoiPokemon.ghiPet(new DataOutputStream(bo), 300, 628, true);
            final byte[] b = bo.toByteArray();
            Kiem.bang("5 byte", Integer.valueOf(5), Integer.valueOf(b.length));
            Kiem.bang("charId big-endian", Integer.valueOf(300), Integer.valueOf(((b[0] & 0xff) << 8) | (b[1] & 0xff)));
            Kiem.bang("dexPet", Integer.valueOf(628), Integer.valueOf(((b[2] & 0xff) << 8) | (b[3] & 0xff)));
            Kiem.bang("shiny", Integer.valueOf(1), Integer.valueOf(b[4]));
            final ByteArrayOutputStream bo2 = new ByteArrayOutputStream();
            GoiPokemon.ghiPet(new DataOutputStream(bo2), 7, 0, false);
            final byte[] b2 = bo2.toByteArray();
            Kiem.bang("khong pet: dex 0", Integer.valueOf(0), Integer.valueOf(((b2[2] & 0xff) << 8) | (b2[3] & 0xff)));
            Kiem.bang("khong pet: shiny 0", Integer.valueOf(0), Integer.valueOf(b2[4]));
        } catch (final Exception e) {
            Kiem.dung("ghiPet: " + e, false);
        }
        try {
            final String g = "../client_pc_ios/Assets/Scripts/Assembly-CSharp/";
            final String mh = new String(Files.readAllBytes(new File(g + "MessageHandler.cs").toPath()), StandardCharsets.UTF_8);
            Kiem.dung("client xu ly case -94", mh.contains("case -94:"));
            Kiem.dung("client doc i16,i16,u8 cho -94", mh.contains("c.pokePetDex = dexPet;"));
            final String am = new String(Files.readAllBytes(new File(g + "AnimalMove.cs").toPath()), StandardCharsets.UTF_8);
            Kiem.dung("AnimalMove ve pet bang PokeBody", am.contains("PokeBody.paint(g, chu.pokePetDex"));
            final String pet = new String(Files.readAllBytes(new File(g + "Pet.cs").toPath()), StandardCharsets.UTF_8);
            Kiem.dung("Pet (pet-tool) cung goi vePokePet", pet.contains("vePokePet("));
        } catch (final Exception e) {
            Kiem.dung("doc client: " + e, false);
        }
    }

    /** taoPokeThan phai tra ve chuoi -90 -> -93 -> -94, va -94 mang dex cua item pet dang dung. */
    private static void kiemChuoiGoi() {
        Kiem.nhom("TienHoaPokemon - chuoi -90 -> -93 -> -94");
        try {
            final real.Char c = new real.Char(null);
            c.id = 321;
            c.pokeDex = 25;
            Kiem.bang("khong pet -> -94 dex 0", Integer.valueOf(0), Integer.valueOf(dexTrongChuoi(c)));

            final data.Pet pet = new data.Pet(0);
            pet.itemPet = new ItemPet(708);
            c.petUsing = pet;
            Kiem.bang("pet item 708 (Crobat cap 5) -> -94 dex 169", Integer.valueOf(169), Integer.valueOf(dexTrongChuoi(c)));
            Kiem.bang("TienHoaPokemon.dexPet", Integer.valueOf(169), Integer.valueOf(real.TienHoaPokemon.dexPet(c)));

            pet.idHoaHinhPet = 67;
            pet.timeEndHoaHinhPet = System.currentTimeMillis() + 60000L;
            Kiem.bang("pet dang hoa hinh Lan -> dex 0 (giu hinh Lan)", Integer.valueOf(0), Integer.valueOf(dexTrongChuoi(c)));
        } catch (final Throwable e) {
            Kiem.dung("chuoi goi: " + e, false);
        }
    }

    /** Dex trong goi -94 cua chuoi taoPokeThan; -1 neu chuoi sai thu tu. */
    private static int dexTrongChuoi(final real.Char c) {
        final io.Message than = real.TienHoaPokemon.taoPokeThan(c);
        if (than == null || than.command != -90 || than.kemTheo == null || than.kemTheo.command != -93) {
            return -1;
        }
        final io.Message pet = than.kemTheo.kemTheo;
        // sau -94 chi con -102 ngoai hinh trang bi (NgoaiHinhGame) la mat xich cuoi
        if (pet == null || pet.command != -94
                || (pet.kemTheo != null && (pet.kemTheo.command != -102 || pet.kemTheo.kemTheo != null))) {
            return -1;
        }
        // toByteArray = u8 lenh, i16 do dai, roi 5 byte noi dung + (0.9.6 the dat ten, DatTenPet.ghiTenPet) u8 phien ban,
        // UTF ten hien, u8 co ten rieng - client cu doc 5 byte dau roi bo qua.
        final byte[] b = pet.toByteArray();
        final int dai = ((b[1] & 0xff) << 8) | (b[2] & 0xff);
        if (b.length != 3 + dai || dai < 5 + 4 || b[0] != (byte) -94 || b[8] != 1
                || dai != 5 + 1 + 2 + (((b[9] & 0xff) << 8) | (b[10] & 0xff)) + 1
                || (((b[3] & 0xff) << 8) | (b[4] & 0xff)) != c.id) {
            return -1;
        }
        return ((b[5] & 0xff) << 8) | (b[6] & 0xff);
    }

    private static final class ItemPet extends real.Item {
        private final real.ItemTemplates template = new real.ItemTemplates();

        ItemPet(final int idTemplate) {
            this.template.id = idTemplate;
            this.template.atb = new short[33];
        }

        @Override
        public real.ItemTemplates getTemplate() {
            return this.template;
        }
    }
}
