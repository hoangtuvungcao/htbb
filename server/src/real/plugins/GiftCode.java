/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package real.plugins;

import data.Database;
import real.Char;
import real.GemTemplate;
import real.Item;
import real.ItemTemplates;
import real.Map;
import static real.Map.doBuyModelClothe;
import static real.Map.getShopTemplate;
import static real.Map.isItemThanThu;
import real.MessageCreator;
import real.OfflineMap;

/**
 *
 *
 */
public class GiftCode {

    public static void putGiftcode(Char player, String code) {
        
    }

    public static void dosetGiftCodeTrianNgude(Char player, String code) {
        for (int i = 0; i < GemTemplate.ID_MATERIAL_LOW[5].length; ++i) {
            player.doAddGemItem(GemTemplate.ID_MATERIAL_LOW[5][i], 2, true);
            player.doAddGemItem(GemTemplate.ID_MATERIAL_HIGHT[5][i], 2, true);
        }
        doBuyModelClothe(player, getShopTemplate(player.isCharNu() ? 50 : 54), 7);
        player.sendMessage(MessageCreator.createCharGemItem(player));
        player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
        player.sendMessage(MessageCreator.createServerAlertMessage("Chúc mừng bạn đã nhận được nlsc, nlcc cấp 6 mỗi loại 2 viên, 1 áo thời trang có thời hạn 7 ngày", ""));
        Database.instance.saveOrtherLog("", player.charname, code + " đã nhận được nlsc, nlcc cấp 6 mỗi loại 2 viên, 1 áo thời trang có thời hạn 7 ngày", "gcode");
    }

    public static void AddItem(Char p, int iditem, int clazz, int magic_vl, int minute3) {
        // Chan tran hanh trang: khong co cho nay thi go giftcode nhieu lan se nhoi
        // hang nghin mon vao iItems, chuoi inven phinh len vai MB va saveCharAuto
        // chet vi PacketTooBig -> nhan vat mat sach tien trinh.
        if (p.isFullInventory()) {
            p.sendMessage(MessageCreator.createServerAlertMessage("Hành trang đã đầy, không nhận thêm được.", ""));
            return;
        }

        ItemTemplates template = Map.itemTemplates.get(clazz).get(iditem);
        Item newItem = null;
        newItem = new Item(template, false, clazz, clazz, template.id);
        newItem.id = p.getIDItem();
        newItem.owner = p.charDBID;
        newItem.level = newItem.getTemplate().level;
        p.iItems.add(newItem);
        newItem.identify = p.charDBID;
        newItem.clazz = (byte) clazz;
        if (template.type < 3 || template.type == 10 || template.type == 11) {
            newItem.magic_physic = p.typeItemBuy;
            if (magic_vl == 0) {
                newItem.atb[6] = newItem.atb[1];
                short[] atb = newItem.atb;
                final int n2 = 1;
                atb[n2] /= 10;
            } else if (magic_vl == 1) {
                newItem.atb[6] = (short) (newItem.atb[1] / 10);
            }
        }
        if (OfflineMap.isAnimalItem(template.type)) {
            newItem.magic_physic = (byte) magic_vl;
            if (newItem.magic_physic < 0) {
                newItem.magic_physic = 2;
            }
            if (magic_vl == 0) {
                newItem.atb[6] = newItem.atb[1];
                short[] atb2 = newItem.atb;
                final int n3 = 1;
                atb2[n3] /= 10;
            } else if (magic_vl == 1) {
                newItem.atb[6] = (short) (newItem.atb[1] / 10);
            }
        }
        if (minute3 > 0) {
            newItem.timeLoan = System.currentTimeMillis();
            newItem.minuteExist = minute3;
        }
        if (isItemThanThu(template.type)) {
            newItem.doAddOptionThanThu(template.atb[0]);
        }
        if (newItem.isVukhiThoiTrang()) {
            newItem.createAttributeVuKhiThoiTrang();
        }
        if (newItem.isCanh()) {
            if (newItem.getTemplate().id == 898) {
                newItem.createOptionCanh(0, 5);
            } else if (newItem.getTemplate().id == 899) {
                newItem.createOptionCanh(1, 5);
            } else if (newItem.getTemplate().id == 900) {
                newItem.createOptionCanh(2, 10);
            } else if (newItem.getTemplate().id == 901) {
                newItem.createOptionCanh(3, 30);
            } else if (newItem.getTemplate().id == 902) {
                newItem.createOptionCanh(4, 50);
            }
        }
        // Skin Kim Cang Long (911): roll ngau nhien 2-3 dong chi so - tu return neu
        // khong phai id 911 nen goi vo dieu kien o day la an toan.
        newItem.createAttributeSkinKimCangLong();

        newItem.dateCreate = Char.getDayTime(0L);
        p.sendMessage(MessageCreator.createCharInventoryMessage(p, 1));
    }

}
