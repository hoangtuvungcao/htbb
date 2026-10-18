package real;

import com.htbb.pokemon.DaTienHoa;

/**
 * Nguon phat da tien hoa Eevee (item 960-967): roi khi Eevee cap >= 25 ha quai.
 * Goi tu dau Monster.onDropItem (moi lan quai chet co nguoi nhan do). Ti le, dieu
 * kien: com.htbb.pokemon.DaTienHoa.daRoiKhiHaQuai (ham thuan, co test).
 */
public final class RoiDaTienHoa {

    private RoiDaTienHoa() {
    }

    public static void khiHaQuai(final Char p, final Monster quai) {
        if (p == null || quai == null || p.lvDetail == null) {
            return;
        }
        try {
            final int idDa = DaTienHoa.daRoiKhiHaQuai(p.pokeDex, p.lvDetail.lv, quai.level,
                    Map.r.nextInt(1000), Map.r.nextInt(DaTienHoa.ID_ITEM.length));
            if (idDa <= 0) {
                return;
            }
            Char.createItemSoLuong(idDa, 1, p);
            p.sendMessage(MessageCreator.createCharInventoryMessage(p, 0));
            final int i = idDa - DaTienHoa.ID_DAU;
            final String ten = (i >= 0 && i < DaTienHoa.TEN.length) ? DaTienHoa.TEN[i] : "đá tiến hoá";
            p.sendMessage(MessageCreator.createServerAlertMessage("Bạn nhặt được " + ten + "! Dùng để Eevee tiến hoá.", ""));
            data.Database.instance.saveOrtherLog("", p.charname, "roi da tien hoa " + idDa + " tu quai " + quai.idTemplate, "datienhoa");
        } catch (final Exception e) {
            System.out.println("RoiDaTienHoa: " + e);
        }
    }
}
