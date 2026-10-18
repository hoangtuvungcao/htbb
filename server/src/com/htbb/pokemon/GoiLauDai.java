package com.htbb.pokemon;

import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Noi dung 5 goi chien dau -95..-99 (THIET_KE_LAU_DAI.md muc 4.6 - BAN CHOT, client J2ME
 * doc o game.pokemon.PokeMangChienDau). Lop THUAN chi ghi byte, big-endian. Moi goi la goi
 * KEM (io.Message.kemTheo) hoac goi doc lap moi; client cu bo qua.
 *
 * So lenh: da grep src/ (16-17/10/2026) - Message(-95..-99) chua dung lam so lenh. "-99" o
 * RequestNPCInfoHandler la id NPC, khac khong gian so voi Message.command.
 */
public final class GoiLauDai {

    public static final int POKE_DON = -95;
    public static final int POKE_CANH_BAO = -96;
    public static final int POKE_TRANG_THAI = -97;
    public static final int POKE_BOSS = -98;
    public static final int POKE_DONG_GOP = -99;

    public static final int LOAI_NGUOI = 0;
    public static final int LOAI_QUAI = 1;

    /** Toi da nguoi trong bang dong gop gui moi 5 giay. */
    public static final int DONG_GOP_TOI_DA = 10;

    private GoiLauDai() {
    }

    /** -95: i16 nguon, u8 loaiNguon, i16 dich, u8 loaiDich, i32 dam, u8 co, u8 heChieu, u8 trangThai. */
    public static void ghiDon(final DataOutputStream d, final int nguon, final int loaiNguon, final int dich,
            final int loaiDich, final int dam, final int co, final int heChieu, final int trangThai) throws IOException {
        d.writeShort(nguon);
        d.writeByte(loaiNguon);
        d.writeShort(dich);
        d.writeByte(loaiDich);
        d.writeInt(dam);
        d.writeByte(co);
        d.writeByte(heChieu < 0 ? 0 : heChieu);
        d.writeByte(trangThai);
    }

    /**
     * -96: i16 boss, u16 idChieu, u8 heChieu, u8 hinh (0 tron,1 non,2 chu nhat,3 vanh),
     * i16 x, i16 y, i16 r_hoac_dai, i16 rong_hoac_goc, i16 huong (do), u16 msNiem.
     */
    public static void ghiCanhBao(final DataOutputStream d, final int boss, final int idChieu, final int heChieu,
            final int hinh, final int x, final int y, final int r, final int rong, final int huong, final int msNiem)
            throws IOException {
        d.writeShort(boss);
        d.writeShort(idChieu);
        d.writeByte(heChieu < 0 ? 0 : heChieu);
        d.writeByte(hinh);
        d.writeShort(x);
        d.writeShort(y);
        d.writeShort(r);
        d.writeShort(rong);
        d.writeShort(huong);
        d.writeShort(Math.max(0, Math.min(65535, msNiem)));
    }

    /** -97: i16 actor, u8 loaiActor, u8 trangThai, u8 chong, u16 msConLai (0 = go). */
    public static void ghiTrangThai(final DataOutputStream d, final int actor, final int loaiActor, final int tt,
            final int chong, final int msConLai) throws IOException {
        d.writeShort(actor);
        d.writeByte(loaiActor);
        d.writeByte(tt);
        d.writeByte(chong);
        d.writeShort(Math.max(0, Math.min(65535, msConLai)));
    }

    /** -98: i16 boss, u8 pha, u8 suKien (0 vao pha,1 no,2 goi de,3 khien), i32 hpConLai, u16 giayDenNo. */
    public static void ghiBoss(final DataOutputStream d, final int boss, final int pha, final int suKien,
            final int hpConLai, final int giayDenNo) throws IOException {
        d.writeShort(boss);
        d.writeByte(pha);
        d.writeByte(suKien);
        d.writeInt(hpConLai);
        d.writeShort(Math.max(0, Math.min(65535, giayDenNo)));
    }

    /** -99: u8 n, n x (i16 charId, u16 phanNghin), u16 phanNghinCuaToi. */
    public static void ghiDongGop(final DataOutputStream d, final int[] charId, final int[] phanNghin, final int n,
            final int cuaToi) throws IOException {
        final int m = Math.max(0, Math.min(Math.min(n, DONG_GOP_TOI_DA), Math.min(charId.length, phanNghin.length)));
        d.writeByte(m);
        for (int i = 0; i < m; i++) {
            d.writeShort(charId[i]);
            d.writeShort(Math.max(0, Math.min(1000, phanNghin[i])));
        }
        d.writeShort(Math.max(0, Math.min(1000, cuaToi)));
    }
}
