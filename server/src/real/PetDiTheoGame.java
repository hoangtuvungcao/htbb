package real;

import data.Database;
import data.Pet;

/**
 * Bam "Su dung" mot ITEM PET da no (data_item type 27: "Cleffa cấp 1", "Hoothoot cấp 1"... - pet dong hanh mien phi /
 * mua o Nha tre, pet no tu trung Nha tre) = dat pet do DI THEO nhan vat (thanh mot data.Pet trong bang Thu nuoi, gan
 * lam petUsing).
 *
 * <p>Bao loi 08/11/2026 (Đào Ngọc Thịnh / rongngoo, 3 item Cleffa cấp 1 trong tui, khong co pet nao): "Với con pet đi
 * theo, làm sao trang bị? Bấm sử dụng nó toàn để vào ấp trứng" - "Ấp xong rồi mà bấm sử dụng vẫn ra cái ấp tiếp". Truoc
 * day Map.doUseItemGoc chi co nhanh TRUNG (type 26, Item.istrungPet -> Char.doCreatePet); item pet type 27 roi xuong
 * DungVatPham.goiY = "gửi ở Menu › Hệ sinh thái › Nhà trẻ và trứng › Gửi pet đang sở hữu" -> nguoi choi bi day ve Nha tre
 * mai, KHONG co duong nao bien item pet (pet dong hanh / pet no o Nha tre) thanh pet di theo.
 *
 * <p>Mau pet (Map.petTemplate) cua item: lan nguoc chuoi idItemUpLevel tu cac TRUNG type 26 (Trứng Cleffa 686: atb1 = 25,
 * idUpLevel 699 -> 700 -> ... -> 703: ca chuoi Cleffa..Clefable la mau 25) - y het Char.doCreatePet (trung -> pet mau
 * atb1 cua trung). Pet khong co trung trong data_item (Pachirisu 727, Litleo 803, Bidoof 873...) dung mau 29 (TYPE_WALK,
 * giong doCreatePetBachThu 727) - hinh ve theo dex cua item (PetPokemon / goi -94), mau chi quyet kieu di. KHONG dung mau
 * 30 (Capybara giao dich duoc) cho item tu tui: item pet trong tui la item KHOA (PetDongHanh), bien thanh pet giao dich
 * duoc la lo hong clone.
 *
 * <p>Pet van tinh "dang so huu" cho Nha tre (HeSinhThaiMenu.petSoHuu doc ca p.pet lan p.iItems).
 */
public final class PetDiTheoGame {

    /** Mau pet di bo cho item pet khong co trung (Pachirisu / Litleo / Growlithe / Deerling / Bidoof...). */
    static final int MAU_MAC_DINH = 29;

    private PetDiTheoGame() {
    }

    static ItemTemplates mau(final int id) {
        try {
            return (ItemTemplates) Map.itemTemplates.get(5).get(Integer.valueOf(id));
        } catch (final Exception e) {
            return null;
        }
    }

    /**
     * Mau pet (chi so Map.petTemplate) cho item pet {@code t} (type 27), -1 neu khong phai item pet / khong co mau.
     * Uu tien trung type 26 co chuoi idItemUpLevel di toi {@code t.id}; khong co thi {@link #MAU_MAC_DINH} cho item co
     * trong PetPokemon (pet Pokemon co hinh).
     */
    public static int mauPet(final ItemTemplates t) {
        if (t == null || t.type != 27) {
            return -1;
        }
        try {
            for (final ItemTemplates e : Map.itemTemplates.get(5).values()) {
                if (e == null || e.type != 26 || e.atb == null || e.atb.length < 2) {
                    continue;
                }
                int id = e.idItemUpLevel;
                for (int buoc = 0; buoc < 8 && id > 0; buoc++) {
                    if (id == t.id) {
                        return hopLe(e.atb[1]) ? e.atb[1] : -1;
                    }
                    final ItemTemplates k = mau(id);
                    if (k == null) {
                        break;
                    }
                    id = k.idItemUpLevel;
                }
            }
        } catch (final Exception e) {
            // bang mau dang nap lai: roi xuong mac dinh
        }
        return com.htbb.pokemon.PetPokemon.dex(t.id) > 0 && hopLe(MAU_MAC_DINH) ? MAU_MAC_DINH : -1;
    }

    static boolean hopLe(final int mau) {
        return Map.petTemplate != null && mau >= 0 && mau < Map.petTemplate.length && Map.petTemplate[mau] != null;
    }

    static boolean chuaCoChiSo(final Item it) {
        return it.newAtb != null && it.newAtb.length > 5
                && it.newAtb[2] == 0 && it.newAtb[3] == 0 && it.newAtb[4] == 0 && it.newAtb[5] == 0;
    }

    /**
     * Dung item pet: true = da xu ly (da cho di theo hoac da bao ly do), false = khong phai item pet dat di theo duoc
     * (duong cu: thong bao huong dan).
     */
    public static boolean dung(final Char p, final Item item) {
        if (p == null || item == null || item.getTemplate() == null || !item.isPet()) {
            return false;
        }
        final ItemTemplates t = item.getTemplate();
        final int mau = mauPet(t);
        if (mau < 0) {
            return false;
        }
        if (QuanLyThuGame.chanNhan(p, "dat item pet di theo")) { // o thu day: da bao ly do
            return true;
        }
        final Pet pet;
        synchronized (p) {
            if (!p.iItems.remove(item)) {
                return true;
            }
            p.removeIDItem(item.id);
            if (chuaCoChiSo(item)) {
                item.doAddOptionPet(t.atb[0]); // nhu Char.doCreatePet: pet cap n co chi so co ban cua cap
            }
            pet = new Pet(Map.petTemplate[mau].idTemplate, Map.petTemplate[mau].idImg, p.getIDItem(), 0, p.charDBID,
                    Map.petTemplate[mau].name, item);
            pet.type = Map.petTemplate[mau].type_move;
            pet.timeBuy = System.currentTimeMillis();
            if (item.minuteExist > 0) {
                pet.totalMinute = item.minuteExist;
                pet.timeBuy = item.timeLoan;
                pet.ispetthoihan = true;
            }
            p.pet.add(pet);
            choDiTheo(p, pet);
        }
        if (Database.instance != null) {
            try {
                Database.instance.savePet(pet);
                Database.instance.saveOrtherLog("tob_log_other_animal", p.charname,
                        "item pet di theo " + item.getInfoSave(), "pet");
                Database.instance.addInfoWebChar(p);
            } catch (final Exception e) {
                e.printStackTrace();
            }
        }
        DungVatPham.bao(p, DungVatPham.ten(item) + " đã ra đi theo bạn (thú nuôi). Xem / đổi pet ở Hành trang › Thú nuôi. "
                + "Pet đi theo vẫn gửi Nhà trẻ được.");
        return true;
    }

    /** Dat {@code pet} lam pet di theo - cung cac buoc cua Map.doCuoiThu (type 1). */
    static void choDiTheo(final Char p, final Pet pet) {
        if (p.petUsing != null) {
            p.petUsing.place = 0;
            if (!p.petUsing.isPetTool() && p.petUsing.getIdEffPet() > -1 && p.petUsing.getIdEffPet() != pet.getIdEffPet()) {
                p.huyEff(p, p.petUsing.getIdEffPet());
            }
        }
        pet.place = 1;
        p.petUsing = pet;
        try {
            p.calculateAttrib();
        } catch (final Exception e) {
            e.printStackTrace();
        }
        // tui (1) + bang thu nuoi (2) truoc: pet moi phai hien ngay ca khi goi sau loi
        try {
            p.sendMessage(MessageCreator.createCharInventoryMessage(p, 1));
            p.sendMessage(MessageCreator.createCharInventoryMessage(p, 2));
            p.sendMessage(MessageCreator.createMainCharInfoMessage(p));
        } catch (final Exception e) {
            e.printStackTrace();
        }
        try {
            p.sendMessage(MessageCreator.createCharWearingMessage(p, p));
            p.sendToNearPlayer(MessageCreator.createCharWearingMessage(p, p));
        } catch (final Exception e) {
            e.printStackTrace();
        }
        if (!pet.isPetTool() && pet.getIdEffPet() > -1
                && p.addEffBuff(pet.getIdEffPet(), System.currentTimeMillis() + 320000000L, EffectBuff.BY_ACTOR, 0) != null) {
            p.sendEffToChar(p);
        }
    }
}
