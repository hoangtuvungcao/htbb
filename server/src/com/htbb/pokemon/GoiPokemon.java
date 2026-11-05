package com.htbb.pokemon;

import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Noi dung 3 lenh mang moi cua che do Pokemon (muc 7.3 docs/kien-truc.md).
 * Big-endian bang DataOutputStream nhu moi goi khac cua game goc; client doc o
 * client_pc_ios MessageHandler.onPokeThan/onPokeQuai/onPokeTienHoa.
 *
 * So lenh -90/-91/-92: da grep src/ va client Unity - chua lenh nao dung lam so
 * lenh. Cho nao trong src/ co -90/-91/-92 la id NPC (vd bot "doi thuong" -90),
 * khong cung khong gian so voi Message.command.
 *
 * Lop THUAN (chi ghi byte), kiem thu o test/KiemPokemonServer.java. Viec dung
 * io.Message va chon nguoi nhan nam o real.TienHoaPokemon / real.MessageCreator.
 */
public final class GoiPokemon {

    /** S->C: i16 charId, i16 dex, u8 shiny. */
    public static final int POKE_THAN = -90;
    /** S->C: u8 n, n x (u8 idTemplate, i16 dex, u8 shiny, u8 tiLe%). */
    public static final int POKE_QUAI = -91;
    /** S->C: i16 charId, i16 dexCu, i16 dexMoi. Gui TRUOC -90. */
    public static final int POKE_TIEN_HOA = -92;

    /** -93 POKE_CUOI: i16 charId, i16 dexThu (0 = khong cuoi), u8 shiny. Gui ngay sau moi -90. */
    public static final int POKE_CUOI = -93;

    /**
     * -94 POKE_PET: i16 charId, i16 dexPet (0 = khong co pet / pet khong phai Pokemon), u8 shiny
     * [+ 0.9.6: u8 phienBan = 1, UTF tenHien, u8 coTenRieng - DatTenPet.ghiTenPet, client cu bo qua].
     * Gui ngay sau moi -93 (chuoi -90 -> -93 -> -94). -94 da grep src/ va client Unity: chua
     * dung lam so lenh (chi co -99 la id NPC o gan).
     */
    public static final int POKE_PET = -94;

    /** Ti le ve quai thuong / boss (%). */
    public static final int TI_LE_THUONG = 100;
    public static final int TI_LE_BOSS = 160;

    /** Template co maxhp tu muc nay tro len coi la boss (ve to). */
    public static final int HP_BOSS = 10000000;

    /**
     * Template do cac lop Boss*.java dung (bang 6.2 cua ke hoach, doi chieu
     * "new Boss...(..., monsterTemplates.get(N)" trong src/): 38 ThuongLuong,
     * 39/82 ThanLan, 48 TrungThu, 90 NguoiTuyet, 91 LinhXa, 92 BachXa, 93 MatQuy,
     * 94/113/115 Skelonton, 116/117 Dracula, 120 TruRong, 129/130 SonTinhThuyTinh.
     * 43 (tru lien hoa) va 46 (cong thanh) la vat the, KHONG phai boss.
     *
     * <p>05/11/2026 (bao loi kazuhas4 o Violet: "quai map ma he thong nhan dien thanh truong Gym", nhan "P3 4:58" tren
     * Arcanine hoang da): BO 48. Template 48 nay la Arcanine QUAI THUONG (data_monster isuse 1, cap 55) moc o ban do
     * ngoai troi - nam trong danh sach nen MOI con Arcanine bi coi la boss (BossLauDai: vao pha 70% / 40%, goi de, dem
     * nguoc no 5 phut nhu boss Gym, tat Tu dong danh). BossTrungThu (ban 48 cu) tu dat isBoss = true nen khong can.
     */
    public static final int[] TEMPLATE_BOSS = {38, 39, 82, 90, 91, 92, 93, 94, 113, 115, 116, 117, 120, 129, 130};

    /**
     * Pokemon gan cho bot NPC (Char isBot) theo chi so - chon loai "than thien":
     * Chansey, Kecleon, Xatu, Psyduck, Jigglypuff.
     */
    public static final int[] DEX_BOT = {113, 352, 178, 54, 39};

    private GoiPokemon() {
    }

    public static boolean laTemplateBoss(final int idTemplate, final int maxhp) {
        if (maxhp >= HP_BOSS) {
            return true;
        }
        for (final int id : TEMPLATE_BOSS) {
            if (id == idTemplate) {
                return true;
            }
        }
        return false;
    }

    public static int tiLe(final int idTemplate, final int maxhp) {
        return laTemplateBoss(idTemplate, maxhp) ? TI_LE_BOSS : TI_LE_THUONG;
    }

    /** Dex cho bot thu {@code chiSo} (am cung duoc - lay tri tuyet doi). */
    public static int dexBot(final int chiSo) {
        final int i = Math.abs(chiSo % DEX_BOT.length);
        return DEX_BOT[i];
    }

    public static void ghiThan(final DataOutputStream dos, final int charId, final int dex, final int shiny) throws IOException {
        dos.writeShort(charId);
        dos.writeShort(dex);
        dos.writeByte(shiny != 0 ? 1 : 0);
    }

    public static void ghiCuoi(final DataOutputStream dos, final int charId, final int dexThu, final boolean shiny) throws IOException {
        dos.writeShort(charId);
        dos.writeShort(dexThu);
        dos.writeByte(shiny ? 1 : 0);
    }

    public static void ghiPet(final DataOutputStream dos, final int charId, final int dexPet, final boolean shiny) throws IOException {
        dos.writeShort(charId);
        dos.writeShort(dexPet);
        dos.writeByte(shiny ? 1 : 0);
    }

    public static void ghiTienHoa(final DataOutputStream dos, final int charId, final int dexCu, final int dexMoi) throws IOException {
        dos.writeShort(charId);
        dos.writeShort(dexCu);
        dos.writeShort(dexMoi);
    }

    /** Template co duoc dua vao goi -91 khong: co dex, va id vua 1 byte. */
    public static boolean coTrongGoiQuai(final int idTemplate, final int dex) {
        return dex > 0 && idTemplate >= 0 && idTemplate <= 255;
    }

    /**
     * Ghi goi -91 tu cac mang song song (moi phan tu la mot template). Chi ghi
     * template coTrongGoiQuai; toi da 255 dong (n la u8).
     *
     * @return so dong da ghi
     */
    public static int ghiQuai(final DataOutputStream dos, final int[] idTemplate, final int[] dex,
            final int[] shiny, final int[] maxhp) throws IOException {
        int n = 0;
        for (int i = 0; i < idTemplate.length && n < 255; i++) {
            if (coTrongGoiQuai(idTemplate[i], dex[i])) {
                n++;
            }
        }
        dos.writeByte(n);
        int daGhi = 0;
        for (int i = 0; i < idTemplate.length && daGhi < n; i++) {
            if (!coTrongGoiQuai(idTemplate[i], dex[i])) {
                continue;
            }
            dos.writeByte(idTemplate[i]);
            dos.writeShort(dex[i]);
            dos.writeByte(shiny[i] != 0 ? 1 : 0);
            dos.writeByte(tiLe(idTemplate[i], maxhp[i]));
            daGhi++;
        }
        return n;
    }
}
