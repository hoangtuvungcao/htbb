package game.pokemon;

import game.model.Char;
import game.model.MonsterTemplate;
import game.render.Canvas;
import game.render.screen.GameScr;
import java.io.DataInputStream;
import network.Message;

/**
 * Lenh mang Pokemon -90 .. -94 (+ -95..-99 chien dau: PokeMangChienDau) (HTBB docs/pokemon/KE_HOACH_CHUYEN_DOI.md muc 7.3) - ban J2ME cua
 * cac ham onPoke* trong ClientUnity MessageHandler.cs. Goi o DAU MessageHandler.onMessage.
 * Server chi gui cac lenh nay khi sv.che_do_pokemon=1; server cu khong gui gi, game ve kieu cu.
 */
public final class PokeMang {
    public static final int POKE_THAN = -90;
    public static final int POKE_QUAI = -91;
    public static final int POKE_TIEN_HOA = -92;
    public static final int POKE_CUOI = -93;
    public static final int POKE_PET = -94;

    private PokeMang() {
    }

    /** Nhan vat chinh hoac nguoi/bot trong danh sach actors (giong lenh 5). */
    private static Char timChar(short id) {
        GameScr gs = Canvas.gameScr;
        if (gs == null) {
            return null;
        }
        if (gs.mainChar != null && gs.mainChar.ID == id) {
            return gs.mainChar;
        }
        Object a = gs.findCharByID(id);
        return a instanceof Char ? (Char) a : null;
    }

    /** true = da xu ly (lenh Pokemon). */
    public static boolean xuLy(Message msg) {
        int lenh = msg.command;
        if (lenh <= PokeMangChienDau.POKE_DON && lenh >= PokeMangChienDau.POKE_DONG_GOP) {
            // -95..-99: goi chien dau (co don, canh bao, trang thai, pha boss, dong gop)
            try {
                return PokeMangChienDau.xuLy(lenh, msg.reader());
            } catch (Throwable e) {
                return true;
            }
        }
        if (lenh > POKE_THAN || lenh < POKE_PET) {
            return false;
        }
        try {
            DataInputStream r = msg.reader();
            switch (lenh) {
                case POKE_THAN: {
                    short id = r.readShort();
                    short dex = r.readShort();
                    boolean shiny = r.readUnsignedByte() != 0;
                    Char c = timChar(id);
                    if (c != null) {
                        c.pokeDex = dex;
                        c.pokeShiny = shiny;
                    }
                    break;
                }
                case POKE_QUAI: {
                    int n = r.readUnsignedByte();
                    for (int i = 0; i < n; i++) {
                        int idTemplate = r.readUnsignedByte();
                        short dex = r.readShort();
                        boolean shiny = r.readUnsignedByte() != 0;
                        int tiLe = r.readUnsignedByte();
                        MonsterTemplate.datPoke(idTemplate, dex, shiny, tiLe);
                    }
                    break;
                }
                case POKE_TIEN_HOA: {
                    short id = r.readShort();
                    short dexCu = r.readShort();
                    short dexMoi = r.readShort();
                    Char c = timChar(id);
                    if (c != null) {
                        c.batDauTienHoa(dexCu, dexMoi);
                        HieuUngDanh.tienHoa(c);   // no sang + tieng tien hoa
                        String tenMoi = LoaiPokemonClient.ten(dexMoi);
                        if (tenMoi.length() == 0) {
                            tenMoi = "#" + dexMoi;
                        }
                        GameScr.addChat(c, "Tiến hóa thành " + tenMoi + "!", 50);
                    }
                    break;
                }
                case POKE_CUOI: {
                    short id = r.readShort();
                    short dexThu = r.readShort();
                    boolean shiny = r.readUnsignedByte() != 0;
                    Char c = timChar(id);
                    if (c != null) {
                        c.pokeThuDex = dexThu;
                        c.pokeThuShiny = shiny;
                    }
                    break;
                }
                case POKE_PET: {
                    short id = r.readShort();
                    short dexPet = r.readShort();
                    boolean shiny = r.readUnsignedByte() != 0;
                    Char c = timChar(id);
                    if (c != null) {
                        c.pokePetDex = dexPet;
                        c.pokePetShiny = shiny;
                    }
                    break;
                }
                default:
                    return false;
            }
        } catch (Throwable e) {
            // goi hong: bo qua, khong lam hong vong nhan tin
        }
        return true;
    }
}
