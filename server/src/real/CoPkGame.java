package real;

import com.htbb.pokemon.CoPk;
import io.Message;

/**
 * Khan PK (binh 14..18) phia GAME - thay nhanh 14..18 cua MapDungThuoc.doUsePotion va real.doUsePotion (hai ban cu
 * giong nhau: "hack khan" -&gt; ten do + 300 nghiep, khan khong thao, sau do bo qua im lang). Luat thuan: com.htbb.pokemon.CoPk.
 * Goi 65 {id, use, loai} giu nguyen (client 0.9.7 PC / APK / J2ME: GameScr.onUseItemPK) -&gt; sua chi can deploy server.
 */
public final class CoPkGame {

    private CoPkGame() {
    }

    /** Nguoi choi dung khan loai (14..18). */
    static void dung(final Char p, final int loai) {
        if (p == null) {
            return;
        }
        try {
            final Map map = p.map;
            if (map != null && (map.mapId == 204 || map.mapId == 205 || map.isMapLoiDai())) {
                DungVatPham.loi(p, CoPk.baoKhuVuc());
                dongBo(p);
                return;
            }
            final long bay = System.currentTimeMillis();
            if (p.pk == 0) {
                if (p.isKiller || p.killer > 0) {
                    DungVatPham.loi(p, CoPk.baoTenDo(p.killer));
                    dongBo(p);
                    return;
                }
                p.pk = (byte) loai;
                p.timeUsePK = bay;
                gui(p, 1, loai);
                DungVatPham.bao(p, CoPk.baoDeo(DungVatPham.tenThuoc(loai)));
                return;
            }
            final int cho = CoPk.giayCho(p.lucDanhNguoiPk, bay);
            if (cho > 0) {
                DungVatPham.loi(p, CoPk.baoCho(cho));
                dongBo(p);
                return;
            }
            final int cu = p.pk;
            p.pk = 0;
            gui(p, 0, cu);
            DungVatPham.bao(p, CoPk.baoThao());
            BanHuongGame.nhip(p); // 0.9.8: thao khan = het ngu canh PvP ngay (lyDoPvp xet pk != 0), client doi che do lien
        } catch (final Exception e) {
            com.htbb.log.Nhat.loi("COPK", "dung khan " + loai, e);
        }
    }

    /** Don danh nguoi choi vua trung (MapDanhNhau / Region): bat dau cho thao khan. */
    public static void daDanhNguoi(final Char p) {
        if (p != null) {
            p.lucDanhNguoiPk = System.currentTimeMillis();
        }
    }

    /** Client cu tu dat co khan truoc khi may chu tra loi (WindowInfoScr): gui lai trang thai that cho rieng minh. */
    static void dongBo(final Char p) {
        try {
            final Message m = new Message(65);
            m.dos.writeShort(p.id);
            m.dos.writeByte(p.pk != 0 ? 1 : 0);
            m.dos.writeByte(p.pk != 0 ? p.pk : 14);
            p.sendMessage(m);
            m.cleanup();
        } catch (final Exception ignored) {
        }
    }

    static void gui(final Char p, final int use, final int loai) throws Exception {
        final Message m = new Message(65);
        m.dos.writeShort(p.id);
        m.dos.writeByte(use);
        m.dos.writeByte(loai);
        p.sendMessage(m);
        p.sendToNearPlayer(m);
        m.cleanup();
    }
}
