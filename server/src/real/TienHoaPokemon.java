package real;

import com.htbb.pokemon.DaTienHoa;
import com.htbb.pokemon.GoiPokemon;
import com.htbb.pokemon.PetPokemon;
import com.htbb.pokemon.ThuPokemon;
import com.htbb.pokemon.LoaiPokemon;
import data.Database;
import io.Message;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

/**
 * Phan "Char/Message" cua che do Pokemon (viec S2, S3, S5, I2 va lenh -90/-91/-92
 * trong docs/kien-truc.md). Logic thuan (bang loai, khac he, ghi
 * byte) nam o com.htbb.pokemon va co test; lop nay chi noi chung vao Char/Session.
 */
public final class TienHoaPokemon {

    private TienHoaPokemon() {
    }

    // ------------------------------------------------------------------ nap / he

    /**
     * Goi luc nap nhan vat (CharNapDuLieu.initPinfo), SAU khi da co charClass va cap.
     * dex == 0 (nhan vat cu, chua chay migration) -> dang khoi dau cua class, tien hoa
     * theo cap. dex != 0 -> van chay dangTheoCap de cap nhay vot (GM/LenhWeb dat cap)
     * ra dung dang. Nhanh Eevee do nguoi choi chon nen dangTheoCap giu nguyen.
     */
    public static void napLoai(final Char p) {
        if (p == null) {
            return;
        }
        final int cap = p.lvDetail != null ? p.lvDetail.lv : 1;
        int dex = p.pokeDex;
        if (dex <= 0) {
            dex = LoaiPokemon.khoiDau(p.charClass);
        }
        if (dex > 0) {
            p.pokeDex = LoaiPokemon.dangTheoCap(dex, cap);
        }
        ganHeTheoLoai(p);
    }

    /** he/he2 cua nhan vat = he cua loai dang mang. */
    public static void ganHeTheoLoai(final Char p) {
        if (p == null || p.pokeDex <= 0) {
            return;
        }
        p.he = (byte) LoaiPokemon.he1(p.pokeDex);
        p.he2 = (byte) LoaiPokemon.he2(p.pokeDex);
    }

    // ------------------------------------------------------------------ tien hoa

    /**
     * Goi ngay sau khoi len cap trong MapXuLyLenh (ca hai ban sao). Tien hoa neu cap
     * moi du moc (bang 5 ke hoach). Tra ve true neu da tien hoa.
     */
    public static boolean kiemTienHoaTheoCap(final Char p) {
        if (p == null || p.pokeDex <= 0 || p.lvDetail == null) {
            return false;
        }
        if (VatPhamDungLauDai.chanTienHoa(p)) {
            return false; // GD2b: Da vinh cuu (do giu) chan tien hoa
        }
        final int moi = LoaiPokemon.dangTheoCap(p.pokeDex, p.lvDetail.lv);
        if (moi == LoaiPokemon.KHONG || moi == p.pokeDex) {
            return false;
        }
        tienHoa(p, moi);
        return true;
    }

    /**
     * Doi loai cua nhan vat thanh {@code dexMoi}: cap nhat dex, he/he2, gui -92 roi -90
     * cho chinh minh va nguoi o gan (Char.sendToNearPlayer - cung duong goi 33 len cap),
     * kem cau chat "... da tien hoa thanh ...". Luu xuong CSDL o lan saveChar ke tiep.
     */
    public static void tienHoa(final Char p, final int dexMoi) {
        if (p == null || dexMoi <= 0 || dexMoi == p.pokeDex) {
            return;
        }
        final int dexCu = p.pokeDex;
        p.pokeDex = dexMoi;
        ganHeTheoLoai(p);

        final Message tienHoa = taoPokeTienHoa(p, dexCu, dexMoi);
        final Message than = taoPokeThan(p);
        String ten = LoaiPokemon.ten(dexMoi);
        if (ten.isEmpty()) {
            ten = "#" + dexMoi;
        }
        // Khong gui them cau chat: client tu hien "Tien hoa thanh X!" khi nhan -92 (MessageHandler.onPokeTienHoa).
        for (final Message m : new Message[]{tienHoa, than}) {
            if (m == null) {
                continue;
            }
            p.sendMessage(m);
            p.sendToNearPlayer(m);
        }
        try {
            Database.instance.saveOrtherLog("", p.charname, "tien hoa " + dexCu + " -> " + dexMoi
                    + " (cap " + (p.lvDetail != null ? p.lvDetail.lv : 0) + ")", "tienhoa");
        } catch (final Exception ignored) {
        }
        HocChieuGame.khiTienHoa(p); // loai moi -> tu hoc chieu cua loai (data_learnset)
    }

    /**
     * Map.doUseItem: dung 1 trong 8 da tien hoa Eevee (viec I2). Tra ve true neu
     * {@code item} la da va da xu ly (ke ca bao loi) - noi goi phai return ngay.
     *
     */
    public static boolean dungDaTienHoa(final Char p, final Item item) {
        if (p == null || item == null || item.getTemplate() == null) {
            return false;
        }
        final int idItem = item.getTemplate().id;
        if (!DaTienHoa.laDa(idItem)) {
            return false;
        }
        final int cap = p.lvDetail != null ? p.lvDetail.lv : 0;
        final int dexMoi = DaTienHoa.ketQuaDung(p.pokeDex, cap, idItem);
        if (dexMoi == LoaiPokemon.KHONG) {
            p.sendMessage(MessageCreator.createServerAlertMessage(DaTienHoa.THONG_BAO_KHONG_DUNG_DUOC, ""));
            return true;
        }
        // Tru vat pham TRUOC (CODING_CONVENTION muc 5). Vector.remove la dong bo va tra
        // false neu luong khac vua lay mat mon nay (giao dich, ban...) -> khong tien hoa.
        if (!p.iItems.remove(item)) {
            return true;
        }
        p.sendMessage(MessageCreator.createCharInventoryMessage(p, 1));
        try {
            Database.instance.saveOrtherLog("tob_log_other_item", p.charname,
                    "dbid" + item.dbid + " " + item.getTemplate().name + " -> dex " + dexMoi, "datienhoa");
        } catch (final Exception ignored) {
        }
        tienHoa(p, dexMoi);
        return true;
    }

    // ------------------------------------------------------------------ bot NPC

    /**
     * Gan dex cho bot NPC trong lang (MapNapTaiNguyen.loadNpcServer) theo chi so,
     * de goi 5 cua bot cung kem -90.
     */
    public static void ganLoaiBot(final Vector<Char> bots) {
        if (bots == null) {
            return;
        }
        for (int i = 0; i < bots.size(); i++) {
            final Char bot = bots.get(i);
            if (bot != null && bot.pokeDex <= 0) {
                bot.pokeDex = GoiPokemon.dexBot(i);
            }
        }
    }

    // ------------------------------------------------------------------ goi tin

    /** -90 POKE_THAN cho nhan vat {@code ch}; null neu chua co loai. */
    public static Message taoPokeThan(final Char ch) {
        if (ch == null || ch.pokeDex <= 0) {
            return null;
        }
        final Message m = taoPokeThan(ch.id, ch.pokeDex, ch.pokeShiny);
        if (m != null) {
            // Chuoi -90 -> -93 -> -94 (Session gui ca chuoi kemTheo ngay sau goi chinh).
            final Message cuoi = taoPokeCuoi(ch);
            final Message pet = taoPokePet(ch);
            // ... -> -102 ngoai hinh trang bi (NgoaiHinhGame): phu kien + vang sang hien cho moi nguoi
            Message duoi = NgoaiHinhGame.tao(ch);
            duoi = DanhHieuGame.noiDuoi(duoi, ch); // ... -> -110 nhan danh hieu dang deo (ten + do hiem) cho moi nguoi
            if (pet != null) {
                pet.kemTheo = duoi;
                duoi = pet;
            }
            if (cuoi != null) {
                cuoi.kemTheo = duoi;
                duoi = cuoi;
            }
            m.kemTheo = duoi;
        }
        return m;
    }

    /**
     * Dex Pokemon cua pet dang di theo {@code ch}: tra theo id template cua petUsing.itemPet
     * (PetPokemon). 0 khi khong co pet, pet kieu cu khong co itemPet, item ngoai bang, hoac
     * pet dang hoa hinh Lan (giu hinh Lan cu de con thay hieu ung hoa hinh).
     */
    public static int dexPet(final Char ch) {
        final int id = idItemPet(ch);
        return id > 0 ? PetPokemon.dex(id) : 0;
    }

    private static int idItemPet(final Char ch) {
        if (ch == null) {
            return 0;
        }
        final data.Pet pet = ch.petUsing;
        if (pet == null || pet.itemPet == null || pet.itemPet.getTemplate() == null || pet.dangHoaHinh()) {
            return 0;
        }
        return pet.itemPet.getTemplate().id;
    }

    /**
     * -94 POKE_PET: pet Pokemon di theo cua {@code ch}. Di kem sau moi -93 (xem taoPokeThan),
     * nen moi duong doi pet da gui lai lenh 3 (deo/doi pet, het han, het hoa hinh) tu cap nhat.
     */
    public static Message taoPokePet(final Char ch) {
        if (ch == null) {
            return null;
        }
        final int id = idItemPet(ch);
        final int dex = id > 0 ? PetPokemon.dex(id) : 0;
        final Message m = new Message(GoiPokemon.POKE_PET);
        try {
            GoiPokemon.ghiPet(m.dos, ch.id, dex, dex > 0 && PetPokemon.shiny(id));
        } catch (final Exception e) {
            return null;
        }
        return m;
    }

    /**
     * Gui -94 cho nguoi o gan {@code p}. Dung o nhung duong doi item pet ma chi gui lenh 3 cho
     * CHINH MINH (Pet.doUpdrage, Pet.doUpdrage2MaxByAdmin): ban than da nhan -94 qua chuoi
     * kemTheo cua lenh 3, nguoi xung quanh thi chua.
     */
    public static void guiPokePetChoNguoiGan(final Char p) {
        if (p == null) {
            return;
        }
        final Message m = taoPokePet(p);
        if (m != null) {
            p.sendToNearPlayer(m);
        }
    }

    /**
     * -93 POKE_CUOI: thu cuoi Pokemon cua {@code ch} (0 khi khong cuoi hoac dang hoa hinh).
     * Di kem sau moi -90, nen len/xuong thu (doCuoiThu/doXuongThuCuoi gui lai lenh 3 + 5)
     * tu cap nhat ma khong can them cho goi.
     */
    public static Message taoPokeCuoi(final Char ch) {
        if (ch == null) {
            return null;
        }
        final data.Animal a = ch.animalRide;
        final boolean cuoi = a != null && a.isHoaHinh != 1;
        final int dex = cuoi ? ThuPokemon.dex(a.idImg) : 0;
        final Message m = new Message(GoiPokemon.POKE_CUOI);
        try {
            GoiPokemon.ghiCuoi(m.dos, ch.id, dex, cuoi && ThuPokemon.shiny(a.idImg));
        } catch (final Exception e) {
            return null;
        }
        return m;
    }

    /**
     * -90 cho bot/NPC (createCharInfoNpc): bot chua duoc gan loai thi lay theo id cho
     * on dinh (cung bot luon ra cung Pokemon).
     */
    public static Message taoPokeThanNpc(final Char ch) {
        if (ch == null) {
            return null;
        }
        final int dex = ch.pokeDex > 0 ? ch.pokeDex : GoiPokemon.dexBot(ch.id);
        return taoPokeThan(ch.id, dex, ch.pokeShiny);
    }

    private static Message taoPokeThan(final int charId, final int dex, final int shiny) {
        final Message m = new Message(GoiPokemon.POKE_THAN);
        try {
            GoiPokemon.ghiThan(m.dos, charId, dex, shiny);
        } catch (final Exception e) {
            return null;
        }
        return m;
    }

    public static Message taoPokeTienHoa(final Char ch, final int dexCu, final int dexMoi) {
        if (ch == null) {
            return null;
        }
        final Message m = new Message(GoiPokemon.POKE_TIEN_HOA);
        try {
            GoiPokemon.ghiTienHoa(m.dos, ch.id, dexCu, dexMoi);
        } catch (final Exception e) {
            return null;
        }
        return m;
    }

    /** -91 POKE_QUAI tu Map.monsterTemplates (chi template co dex > 0); null neu tat. */
    public static Message taoPokeQuai() {
        if (Map.monsterTemplates == null) {
            return null;
        }
        final List<MonsterTemplate> ds = new ArrayList<>();
        for (final MonsterTemplate mt : Map.monsterTemplates.values()) {
            if (mt != null) {
                ds.add(mt);
            }
        }
        Collections.sort(ds, (a, b) -> Integer.compare(a.id, b.id));
        final int n = ds.size();
        final int[] id = new int[n];
        final int[] dex = new int[n];
        final int[] shiny = new int[n];
        final int[] maxhp = new int[n];
        for (int i = 0; i < n; i++) {
            final MonsterTemplate mt = ds.get(i);
            id[i] = mt.id;
            dex[i] = mt.dex;
            shiny[i] = mt.shiny;
            maxhp[i] = mt.maxhp;
        }
        final Message m = new Message(GoiPokemon.POKE_QUAI);
        try {
            GoiPokemon.ghiQuai(m.dos, id, dex, shiny, maxhp);
        } catch (final Exception e) {
            return null;
        }
        return m;
    }
}
