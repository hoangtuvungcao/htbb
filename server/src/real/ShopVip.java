/*
 * PHUC HOI TU LOI CU (P3): lop nay duoc dich nguoc tu lib/htbb-core.jar - ma loi da bien dich cua
 * game goc - bang Vineflower 1.11.1 roi sua tay toi thieu cho bien dich duoc (--release 8). Giu
 * nguyen ten lop, package va hanh vi; doi ten / don dep de o giai doan sau. Nguon goc: docs/giay-phep.md.
 */
package real;

import io.Message;
import java.util.Hashtable;
import java.util.Vector;

public class ShopVip {
    public static Hashtable<Integer, CharSellVip> charSeller = new Hashtable<>();
    public static Vector<Vector<Vector<Item>>> ALL_ITEM_SELL;
    static byte[] coloritem;

    static {
        for (int i = 0; i < 20; i++) {
            Vector<Vector<Item>> coloritem = new Vector<>();

            for (int j = 0; j < 4; j++) {
                coloritem.add(new Vector<>());
            }

            ALL_ITEM_SELL.add(coloritem);
        }

        ShopVip.coloritem = new byte[]{0, 3, 2, 1};
    }

    public static void doSellItemVip(Char player, Message msg) {
        try {
            short idItem = msg.dis.readShort();
            int price = msg.dis.readInt();
            if (price < 0) {
                return;
            }

            Item item = player.getItemFormVector(player.iItems, idItem);
            if (item.lock == 1) {
                return;
            }

            if (item != null && !item.isSelling) {
                item.myChar = player.charname;
                item.isSelling = true;
                item.prizeSell = price;
                ALL_ITEM_SELL.get(item.getTemplate().type).get(item.colorName).add(item);
            }
        } catch (Exception var5) {
        }
    }

    public static void doSearchItem(Char player, Message msg) {
        try {
            player.allItemBuy.removeAllElements();
            byte type = msg.dis.readByte();
            short level = msg.dis.readShort();
            byte pham = msg.dis.readByte();
            byte colorItem = msg.dis.readByte();
            Vector<Item> it = ALL_ITEM_SELL.get(type).get(coloritem[colorItem]);

            for (int i = 0; i < it.size(); i++) {
                Item item = it.get(i);
                if (item.level == level && (item.hangItem == pham || colorItem == 0 && item.hangItem == -1)) {
                    player.allItemBuy.add(item);
                }
            }
        } catch (Exception var9) {
        }
    }

    public static void doSendListItem(Char p, int idStart) {
        try {
            int size = 10;
            if (idStart > p.allItemBuy.size()) {
                idStart = 0;
            }

            Message msg = new Message(-58);
            msg.dos.writeByte(1);
            if (size + idStart > p.allItemBuy.size()) {
                size = p.allItemBuy.size() - idStart;
            }

            msg.dos.writeShort(size);

            for (int i = idStart; i < p.allItemBuy.size(); i++) {
                Item it = p.allItemBuy.get(i);
                msg.dos.writeByte(it.clazz);
                msg.dos.writeShort(it.id);
                msg.dos.writeShort(it.getTemplate().id);
                msg.dos.writeByte(it.plus);
                msg.dos.writeInt(it.prizeSell);
                msg.dos.writeByte(it.level);
                msg.dos.writeByte(it.nhadSock);
                msg.dos.writeByte(it.nSocAdd);
                Vector<InfoItemAttribute> allAtb = it.getInfoAtbItem();
                msg.dos.writeByte(allAtb.size());

                for (byte j = 0; j < allAtb.size(); j++) {
                    InfoItemAttribute info = allAtb.get(j);
                    msg.dos.writeByte(info.id);
                    msg.dos.writeShort(info.value);
                }

                msg.dos.writeByte(it.colorName);
                msg.dos.writeByte(it.heItem);
                msg.dos.writeByte(it.hangItem);
                msg.dos.writeByte(it.magic_physic);
                msg.dos.writeUTF(it.myChar);
            }
        } catch (Exception var9) {
        }
    }
}
