package game.pokemon;

import game.model.Char;
import game.model.MonsterTemplate;
import game.render.Canvas;
import game.render.screen.GameScr;
import java.io.DataInputStream;
import network.Message;

/**
 * Lenh mang Pokemon -90 .. -94 (+ -95..-99 chien dau: PokeMangChienDau) (HTBB docs/kien-truc.md) - ban J2ME cua
 * cac ham onPoke* trong Unity MessageHandler.cs. Goi o DAU MessageHandler.onMessage.
 * Server HTBB luon gui cac lenh nay; Char/quai chua nhan dex (= 0) thi game ve kieu cu.
 */
public final class PokeMang {
    public static final int POKE_THAN = -90;
    public static final int POKE_QUAI = -91;
    public static final int POKE_TIEN_HOA = -92;
    public static final int POKE_CUOI = -93;
    public static final int POKE_PET = -94;
    /** S->C 0.9.6: u8 kiểu (0 thành công, 1 lỗi), UTF tiêu đề, UTF nội dung (server com.htbb.pokemon.DatTenPet). */
    public static final int DAT_TEN_KET_QUA = -106;
    /** -102 ngoai hinh trang bi (S->C, NgoaiHinhCua); -103 cai dat an/hien (C->S, NgoaiHinhMenu). */
    public static final int POKE_NGOAI_HINH = -102;

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
        if (lenh == LoaiMang.POKE_QUAI_CON || lenh == LoaiMang.POKE_LOAI) {
            // 1.0: loai rieng tung con quai, sprite tai theo nhu cau, bang loai + Pokedex (LoaiMang)
            return LoaiMang.xuLy(msg);
        }
        if (lenh <= PokeMangChienDau.POKE_DON && lenh >= PokeMangChienDau.POKE_DONG_GOP) {
            // -95..-99: goi chien dau (co don, canh bao, trang thai, pha boss, dong gop)
            try {
                return PokeMangChienDau.xuLy(lenh, msg.reader());
            } catch (Throwable e) {
                return true;
            }
        }
        if (lenh == POKE_NGOAI_HINH) {
            try {
                DataInputStream r = msg.reader();
                short id = r.readShort();
                NgoaiHinhCua n = NgoaiHinhCua.doc(r);
                NgoaiHinhMenu.nap();
                Char c = timChar(id);
                if (c != null) {
                    c.ngoaiHinh = n;
                }
            } catch (Throwable e) {
                // goi hong: bo qua
            }
            return true;
        }
        if (lenh == DAT_TEN_KET_QUA) {
            // 0.9.6 thẻ đặt tên Pokémon: kết quả (server quay dòng) -> thẻ trắng kiểu Pokémon GO (HopThongBao)
            try {
                DataInputStream r = msg.reader();
                int kieu = r.readUnsignedByte();
                String tieuDe = r.readUTF();
                String noiDung = r.readUTF();
                HopThongBao.mo(tieuDe, noiDung, kieu == 0 ? HopThongBao.THONG_TIN : HopThongBao.LOI, new String[]{"OK"},
                        new int[]{Bang.XAC_NHAN}, new game.model.IAction[]{null}, null);
            } catch (Throwable e) {
                // gói hỏng: bỏ qua
            }
            return true;
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
                        GameScr.addChat(c, "Tiến hoá thành " + tenMoi + "!", 50);
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
                    // 0.9.6 đuôi -94 (thẻ đặt tên): u8 phiên bản, UTF tên hiện, u8 có tên riêng; server cũ không gửi
                    String tenPet = null;
                    if (r.available() > 0 && r.readUnsignedByte() >= 1) {
                        tenPet = r.readUTF();
                        if (tenPet.length() == 0) {
                            tenPet = null;
                        }
                    }
                    Char c = timChar(id);
                    if (c != null) {
                        c.pokePetDex = dexPet;
                        c.pokePetShiny = shiny;
                        c.pokePetTen = tenPet;
                        TrangThaiNguoiChoi.doi(TrangThaiNguoiChoi.PET);
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
