package game.pokemon;

import game.model.Actor;
import game.model.AnimalMove;
import game.model.Char;
import game.model.Tilemap;
import game.model.monster.Pet;
import game.render.Canvas;
import game.render.screen.GameScr;

/**
 * NHÃN TRÊN ĐẦU PET (0.9.6, thẻ đặt tên): "tên pet · tên chủ" cho MỌI pet đang đi theo (của mình và người khác), vd
 * "Pikachu · vantron" hoặc "Sấm Sét · vantron" khi pet có tên từ thẻ đặt tên (loài xem ở thẻ thông tin pet).
 *
 * Tên pet lấy từ đuôi gói -94 (Char.pokePetTen, server gửi cho chủ và người ở gần); server cũ không gửi thì dùng tên loài
 * client tự biết (LoaiPokemonClient), không biết loài thì không vẽ. Nhãn đi qua {@link NhanTheGioi} (loại PET): chữ NHỎ
 * và MỜ hơn tên người chơi, xếp chỗ sau tên người chơi, chồng nhãn đã đặt thì nhích lên một hàng, vẫn chồng thì BỎ (không
 * bao giờ đè tên chủ / người khác), không đè hình nhân vật chính. Máy J2ME không có chữ thật: không vẽ (giữ như cũ).
 * Thú cưỡi luôn vẽ dưới người cưỡi (không đứng riêng) nên đi theo hàng tên của người cưỡi.
 */
public final class NhanPet {
    private NhanPet() {
    }

    /** AnimalMove.paintName (lớp thế giới): ghi yêu cầu nhãn của pet a. */
    public static void ve(AnimalMove a) {
        if (a == null || a.catagory != 12 || a.wantDestroy || !NhanTheGioi.bat() || Tilemap.isOfflineMap) {
            return;
        }
        GameScr gs = Canvas.gameScr;
        if (gs == null || gs.hideGUI == 2) {
            return;
        }
        if (!(a instanceof Pet) && a.status == 3) {
            return;   // pet đang mờ dần biến mất (đã cất)
        }
        Char chu = chuCua(a, gs);
        if (chu == null || chu.name == null || chu.name.length() == 0) {
            return;
        }
        if (chu.petCanh != null && chu.petCanh != a) {
            return;   // bản thừa (PetDuyNhat): chỉ pet đang trong cảnh của chủ có nhãn
        }
        int dex = chu.pokePetDex;
        boolean shiny = chu.pokePetShiny;
        if (dex <= 0 && !(a instanceof Pet) && a.type != 5) {
            dex = AnimalMove.dexTheoIdImgPetCu(a.imgID);
            shiny = false;
        }
        String ten = chu.pokePetTen;
        if (ten == null && dex > 0) {
            ten = LoaiPokemonClient.ten(dex);
        }
        if (ten == null || ten.length() == 0) {
            return;
        }
        int cao = 0;
        if (dex > 0 && PokeBody.coSprite(dex, shiny)) {
            cao = PokeBody.caoHinh(dex, shiny) * AnimalMove.TI_LE_POKE_PET / 100;
        }
        if (cao <= 0) {
            cao = a.height > 0 ? a.height : 20;
        }
        NhanTheGioi.pet(a, nhan(chu, ten), a.x, a.y - cao - 1, a.y);
    }

    /** Chủ của pet: myOwner, rồi theo id (pet kiểu 5 mang id chủ; AnimalMove mang idSameMyOwnerID). */
    static Char chuCua(AnimalMove a, GameScr gs) {
        if (a.myOwner != null) {
            return a.myOwner;
        }
        short id = a instanceof Pet ? a.ID : a.idSameMyOwnerID;
        if (gs.mainChar != null && gs.mainChar.ID == id) {
            return gs.mainChar;
        }
        Actor c = gs.findCharByID(id);
        return c instanceof Char && !(c instanceof AnimalMove) ? (Char) c : null;
    }

    /** "tên · chủ" nhớ trên Char chủ (chỉ dựng lại khi tên pet / tên chủ đổi). */
    static String nhan(Char chu, String ten) {
        if (chu.nhanPet != null && ten.equals(chu.nhanPetTenCu) && chu.name.equals(chu.nhanPetChuCu)) {
            return chu.nhanPet;
        }
        chu.nhanPetTenCu = ten;
        chu.nhanPetChuCu = chu.name;
        chu.nhanPet = new StringBuffer(ten.length() + chu.name.length() + 3).append(ten).append(" · ").append(chu.name).toString();
        return chu.nhanPet;
    }
}
