/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ThanKhi;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;
import real.Char;
import real.Item;
import real.ItemTemplates;
import real.Map;
import static real.Map.itemTemplates;
import real.MessageCreator;

/**
 *
 * @author ADMIN
 */
public class ThoRenThanBi 
{
    public static final short[]  startIDMaterial  = new short[] { 68, 75, 82, 89, 96, 103, 110, 117, 124, 131, 149 };
    public static final int[]    amountMaterial   = new int[]   { 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1 };
    public static final String[] nameMaterial     = new String[] 
    {
        "Vải", "Sắt", "Ngọc", "Gỗ thường", "Da mềm", "Tơ lụa", "Bạc", "Thuỷ tinh", "Gỗ sưa", "Da cứng", "Đá ngũ hợp tinh khiết"
    };

    public static final short[]  item_attribute_ids   = new short[] { 163, 164, 165, 166, 167 };
    public static final String[] item_attribute_names = new String[] 
    {
        "Sát thương hệ kim", "Sát thương hệ mộc", "Sát thương hệ thuỷ", "Sát thương hệ hoả", "Sát thương hệ thổ"
    };

    public static final short[]  item_upgrade_material_ids   = new short[] { 169, 170, 171, 172, 173, 174, 175, 176, 177, 178, 179 };
    public static final String[] item_upgrade_material_names = new String[] 
    {
        "Vải", "Sắt", "Ngọc", "Gỗ thường", "Da mềm", "Tơ lụa", "Bạc", "Thuỷ tinh", "Gỗ sưa", "Da cứng", "Đá ngũ hợp"
    };
    
    public static final double[][] HIDDEN_STAT_TABLE = new double[][]
    {
        {180, 230, 280, 350, 420, 490, 560, 650, 740, 900}, // 0   Tăng điểm sát thương vật lý
        {180, 230, 280, 350, 420, 490, 560, 650, 740, 900}, // 1   Tăng điểm sát thương ma pháp
        {10,   20,  30,  40,  50,  80, 120, 160, 200, 250}, // 2   Tăng sát thương chí mạng (%)
        {10,   15,  20,  25,  30,  35,  40,  45,  50,  60}, // 3   Tăng sát thương theo % HP kẻ địch
        {10,   15,  20,  25,  30,  35,  40,  45,  50,  60}, // 4   Tăng sát thương theo % MP kẻ địch
        {3,     6,   9,  13,  18,  24,  30,  36,  43,  50}, // 5   Tăng % sát thương vật lý
        {3,     6,   9,  13,  18,  24,  30,  36,  43,  50}, // 6   Tăng % sát thương ma pháp
        {10,   15,  20,  25,  30,  35,  40,  45,  50,  60}, // 7   Tăng % Xuyên thủ ma
        {10,   15,  20,  25,  30,  35,  40,  45,  50,  60}, // 8   Tăng % Xuyên thủ vật
        {10,   20,  30,  40,  50,  80, 120, 160, 200, 250}, // 9   Tăng % sát thương lên boss/trụ
        {20,   40,  70, 100, 130, 190, 250, 300, 400, 500}, // 10  Tăng % sát thương lên quái
        {10,   20,  30,  40,  50,  70, 100, 130, 160, 200}, // 11  Giảm % sát thương nhận vào từ Boss
        {10,   20,  30,  40,  50,  70, 100, 130, 160, 200}, // 12  Giảm % sát thương nhận vào từ người chơi
        {10,   20,  30,  40,  60,  80, 100, 120, 150, 180}, // 13  Tăng điểm phòng thủ cơ bản
        {2,     4,   6,   8,  10,  12,  14,  16,  18,  20}, // 14  Kháng Hỏa (%)
        {2,     4,   6,   8,  10,  12,  14,  16,  18,  20}, // 15  Kháng Thủy (%)
        {2,     4,   6,   8,  10,  12,  14,  16,  18,  20}, // 16  Kháng Mộc (%)
        {2,     4,   6,   8,  10,  12,  14,  16,  18,  20}, // 17  Kháng Kim (%)
        {2,     4,   6,   8,  10,  12,  14,  16,  18,  20}, // 18  Kháng Thổ (%)
        {10,   20,  30,  40,  50,  60,  70,  80,  90, 100}, // 19  Kháng tất cả (%)
        {20,   30,  40,  50,  70,  90, 110, 130, 150, 180}, // 20  Tăng điểm giáp khi ở tổ đội
        {1,     2,   3,   4,   5,   6,   7,   8,   9,  10}, // 21  Hút HP (%)
        {1,     2,   3,   4,   5,   6,   7,   8,   9,  10}, // 22  Hút MP (%)
        {2,     4,   6,   8,  10,  12,  14,  16,  18,  20}, // 23  Tăng tốc độ đánh (giảm hồi chiêu)
        {1,     1,   1,   1,   1,   1,   1,   1,   1,   1}, // 24  Tăng tốc độ di chuyển (+1 cố định)
        {2,     4,   6,   8,  10,  12,  14,  16,  18,  20}, // 25  Tăng tỉ lệ né tránh
        {2,     4,   6,   8,  10,  12,  14,  16,  18,  20}, // 26  Tăng tỉ lệ phản đòn
        {20,   40,  60,  80, 100, 120, 140, 160, 180, 200}, // 27  Giảm sức mạnh + phòng thủ của kẻ địch
        {2,     4,   6,   8,  10,  12,  14,  16,  18,  20}, // 28  Tăng tỉ lệ Câm lặng của bản thân
    };
    
    // Bảng ánh xạ id cho từng chỉ số ẩn (0..28)
    public static final short[] HIDDEN_STAT_IDS = new short[]
    {
        // Truoc day dung id 140/139, nhung 140 = "Tri Mang" va 139 = "HP" la
        // CHI SO THAT cua trang bi (Item.java ghi vao otherAtt[79]/[78]), nen
        // than khi vua hien sai ten vua cong nham HP/chi mang cho nhan vat.
        // Da chuyen sang 2 o du phong 158/159.
        158, // 0  Tăng điểm sát thương vật lý  -> id 158 "Tăng sát thương vật lý"
        159, // 1  Tăng điểm sát thương ma pháp -> id 159 "Tăng sát thương ma pháp"
        141, // 2  Tăng sát thương chí mạng (%) -> id 141 "Sát thương chí mạng"
        142, // 3  Tăng sát thương theo % HP kẻ địch -> id 142 "Sát thương theo hp"
        143, // 4  Tăng sát thương theo % MP kẻ địch -> id 143 "Sát thương theo mp"
        144, // 5  Tăng % sát thương vật lý -> id 144 "Sát thương vật" (theo %)
        145, // 6  Tăng % sát thương ma pháp -> id 145 "Sát thương ma" (theo %)
        146, // 7  Tăng % Xuyên thủ ma -> id 146 "Xuyên thủ ma"
        147, // 8  Tăng % Xuyên thủ vật -> id 147 "Xuyên thủ vật"
        148, // 9  Tăng % sát thương lên boss/trù -> id 148 "Tăng sát thương boss"
        149, // 10 Tăng % sát thương lên quái -> id 149 "Tăng sát thương quái"
        150, // 11 Giảm % sát thương nhận vào từ Boss -> id 150 "Giảm sát thương boss"
        180, // 12 Giảm % sát thương nhận vào từ người chơi -> id 180 "Giảm sát thương cuối" (tạm dùng)
        151, // 13 Tăng điểm phòng thủ cơ bản -> id 151 "Tăng thủ cơ bản"
        155, // 14 Kháng Hỏa -> id 155 "Kháng hoả"
        154, // 15 Kháng Thủy -> id 154 "Kháng thuỷ"
        153, // 16 Kháng Mộc -> id 153 "Kháng mộc"
        152, // 17 Kháng Kim -> id 152 "Kháng kim"
        156, // 18 Kháng Thổ -> id 156 "Kháng thổ"
        186, // 19 Kháng tất cả -> id 186 "Kháng tất cả"
        157, // 20 Tăng điểm giáp khi ở tổ đội -> id 157 "Tăng kháng khi party"
        182, // 21 Hút HP -> id 182 "Hút hp"
        181, // 22 Hút MP -> id 181 "Hút mp"
        160, // 23 Tăng tốc độ đánh (giảm hồi chiêu) -> id 160 "Giảm cooldown"
        161, // 24 Tăng tốc độ di chuyển -> id 161 "Tốc độ"
        183, // 25 Tăng tỉ lệ né tránh -> id 183 "Tăng né tránh"
        185, // 26 Tăng tỉ lệ phản đòn -> id 185 "Phản đòn"
        184, // 27 Giảm sức mạnh + phòng thủ của kẻ địch -> id 184 "Giảm st và thủ"
        162  // 28 Tăng tỉ lệ Câm lặng -> id 162 "Tăng câm lặng"
    };
    
    public static int[] RandomBaseMaterialIndices() 
    {
      java.util.List<Integer> pickedIdx = new java.util.ArrayList<>(5);

      // Lấy 2 chỉ số trong khoảng 0..4
      while (pickedIdx.size() < 2)
      {
        int idx = java.util.concurrent.ThreadLocalRandom.current().nextInt(5); // 0..4
        if (!pickedIdx.contains(idx)) pickedIdx.add(idx);
      }

       // Lấy 2 chỉ số trong khoảng 5..9
         while (pickedIdx.size() < 4) 
        {
           int idx = 5 + java.util.concurrent.ThreadLocalRandom.current().nextInt(5); // 5..9
            if (!pickedIdx.contains(idx)) pickedIdx.add(idx);
        }

          // Luôn thêm 10
        pickedIdx.add(10);

        // Chuyển sang mảng nguyên thủy
        int[] result = new int[pickedIdx.size()];
        for (int i = 0; i < pickedIdx.size(); i++) 
        {
            result[i] = pickedIdx.get(i);
        }
        return result;
    }
    
    public static void Option(Char player, int idNpc, int idMenu, int idOptionMenu) throws IOException
    {
        if (idMenu == 0)
        {
            if (idOptionMenu == 0) 
            {
                String[] menu = new String[] {"Quạt ba tiêu","Gậy như ý","Lưỡng nhận đao" };
                player.sendMessage(MessageCreator.createMsgMenu(menu, -517, 50));
            }
            else if (idOptionMenu == 1)
            {
                String[] menu = new String[] {"Nâng cấp thường ","Nâng cấp thường - nguyên liệu khóa", "Nâng cấp thường - nguyên liệu tổng hợp",
                                     "Dùng Hỏa thạch cấp 1 (1% tc)", "Dùng Hỏa thạch cấp 1 (1% tc) - nguyên liệu khóa", "Dùng Hỏa thạch cấp 1 (1% tc) - nguyên liệu tổng hợp",
                                     "Dùng Hỏa thạch cấp 2 (10% tc)", "Dùng Hỏa thạch cấp 2 (10% tc) - nguyên liệu khóa", "Dùng Hỏa thạch cấp 2 (10% tc) - nguyên liệu tổng hợp",};
                player.sendMessage(MessageCreator.createMsgMenu(menu, -517, 61));
            }
            else if (idOptionMenu == 2)
            {
                String[] menu = new String[] { "Nguyên liệu không khóa","Nguyên liệu khóa","Nguyên liệu hỗn hợp" };
                player.sendMessage(MessageCreator.createMsgMenu(menu, -517, 62));
            }
            else if (idOptionMenu == 3)
            {
                if (player.itemThanKhi != null) 
                {
                    player.iItems.add(player.itemThanKhi);
                }
                player.itemThanKhi = null;
                player.calculateAttrib();
                player.sendMessage(MessageCreator.createMainCharInfoMessage(player));
                player.sendMessage(MessageCreator.createCharInventoryMessage(player, 1));
                player.sendMessage(MessageCreator.createCharWearingMessage(player, player));
                player.sendToNearPlayer(MessageCreator.createCharInfo(player));
                
                final Vector<Item> wItems = player.getCharWearing();
                for (int i = 0; i < wItems.size(); ++i) 
                {
                    final Item item = wItems.get(i);
                    if (item.getTemplate().type > 2 && item.getTemplate().type < 8) 
                    {
                        player.sendMessage(MessageCreator.createCharWeaponeData(1, item.getTemplate().type, item.getTemplate().style, item.getTemplate().color));
                        return;
                    }
                }
            }
        }
        else if (idMenu == 50)
        {
            if (idOptionMenu < 0 || idOptionMenu > 2) return;
            player.thanKhiCraftState.setItemType((byte)idOptionMenu);
            String[] menu = new String[] { "Nguyên liệu không khóa","Nguyên liệu khóa","Nguyên liệu hỗn hợp" };
            player.sendMessage(MessageCreator.createMsgMenu(menu, -517, 55));
        }
        else if (idMenu == 55)
        {
            if (idOptionMenu < 0 || idOptionMenu > 2) return;
            player.thanKhiCraftState.setMaterialMode((byte)idOptionMenu);
            String[] menu55 = new String[]
            {
                "Dùng nguyên liệu cấp 1", "Dùng nguyên liệu cấp 2", "Dùng nguyên liệu cấp 3", "Dùng nguyên liệu cấp 4", "Dùng nguyên liệu cấp 5", "Dùng nguyên liệu cấp 6"
            };
            player.sendMessage(MessageCreator.createMsgMenu(menu55, -517, 51));
        }
        else if (idMenu == 51)
        {  
            if (idOptionMenu < 0 || idOptionMenu > 5) return;
            if ( player.thanKhiCraftState.getItemType() == -1)
            {
                player.sendMessage(MessageCreator.createServerAlertMessage("Hãy chọn loại thần khí trước khi chọn nguyên liệu", ""));
                return;
            }
            player.thanKhiCraftState.setMaterialLevel((byte)(idOptionMenu + 1));
            int[] baseIdxList = new int[5];
            for (int k = 0; k < 5; k++) 
            {
                baseIdxList[k] = player.thanKhiCraftState.getBaseIdxByItemType()[player.thanKhiCraftState.getItemType()][k];
            }
            short[] randomMaterials = new short[5];
            for (int k = 0; k < 5; k++) 
            {
                int idx = baseIdxList[k];
                randomMaterials[k] = (short) (startIDMaterial[idx] + idOptionMenu);
            }
            player.thanKhiCraftState.setRandomMaterials(randomMaterials);

            int[] requiredAmounts = new int[5];
            for (int k = 0; k < 5; k++) 
            {
                int idx = baseIdxList[k];
                requiredAmounts[k] = amountMaterial[idx];
            }
            String itemName;
            switch (player.thanKhiCraftState.getItemType()) 
            {
                case 0: itemName = "Quạt ba tiêu"; break;
                case 1: itemName = "Gậy như ý"; break;
                default: itemName = "Lưỡng nhận đao"; break;
            }

            java.util.List<String> materialLines = new java.util.ArrayList<>();
            for (int k = 0; k < randomMaterials.length; k++) 
            {
                short matId = randomMaterials[k];
                int req = requiredAmounts[k];
                String matName = Map.gemTemplate[matId].name;
                materialLines.add(req + " " + matName);
            }

            String materialList = String.join(", ", materialLines);
            String confirm = "Tạo thần khí " + itemName + " cần\n" + materialList + "\nNgươi có đồng ý không?";
    
            player.sendMessage(MessageCreator.createMsgPopUp(player.id, 121, confirm));
        }
        else if (idMenu == 61)
        {
            if (idOptionMenu < 0 || idOptionMenu > 8) return;
            player.thanKhiCraftState.setUpgradeSelection((byte) idOptionMenu);
            Vector<Item>  listThanKhi = new Vector<Item>();
            java.util.ArrayList<Short> tmpIds = new java.util.ArrayList<>(listThanKhi.size());
            for (int i = 0; i < player.iItems.size(); i++)
            {
                Item item = (Item) player.iItems.get(i);
                if (item == null) continue;

                if (item.tempateID == 890 || item.tempateID == 891 || item.tempateID == 892)
                {
                    if (Map.isItemSelled(player, item.id, item))
                    {
                        continue;
                    }
                    listThanKhi.add(item);
                    tmpIds.add(item.id);
                }
            }
            short[] upgradeIds = new short[tmpIds.size()];
            for (int z = 0; z < tmpIds.size(); z++) upgradeIds[z] = tmpIds.get(z);
            player.thanKhiCraftState.setUpgradeItemIds(upgradeIds);
            player.sendMessage(MessageCreator.createMessageShopNew(player, listThanKhi, "chọn", "Nâng cấp thần khí", Map.TYPE_NANG_CAP_THAN_KHI));
        }
        else if (idMenu == 62)
        {
            if (idOptionMenu < 0 || idOptionMenu > 2) return;
            player.thanKhiCraftState.setUpgradeSelection((byte) idOptionMenu);
            Vector<Item>  listThanKhi = new Vector<Item>();
            java.util.ArrayList<Short> tmpIds = new java.util.ArrayList<>(listThanKhi.size());
            for (int i = 0; i < player.iItems.size(); i++)
            {
                Item item = (Item) player.iItems.get(i);
                if (item == null) continue;

                if (item.tempateID == 890 || item.tempateID == 891 || item.tempateID == 892)
                {
                    if (Map.isItemSelled(player, item.id, item))
                    {
                        continue;
                    }
                    listThanKhi.add(item);
                    tmpIds.add(item.id);
                }
            }
            short[] upgradeIds = new short[tmpIds.size()];
            for (int z = 0; z < tmpIds.size(); z++) upgradeIds[z] = tmpIds.get(z);
            player.thanKhiCraftState.setUpgradeItemIds(upgradeIds);
            player.sendMessage(MessageCreator.createMessageShopNew(player, listThanKhi, "chọn", "Thay đổi thuộc tính", Map.TYPE_DOI_DONG_THAN_KHI));
        }
    }
    
    public static void CreateThanKhi(Char player) 
    {
        if (player.thanKhiCraftState.getItemType() == -1 || player.thanKhiCraftState.getMaterialMode() == -1 || player.thanKhiCraftState.getMaterialLevel() == 0 || player.thanKhiCraftState.getRandomMaterials().length != 5) 
        {
            player.sendMessage(MessageCreator.createServerAlertMessage("Lỗi trạng thái tạo Thần Khí, vui lòng thử lại", ""));
            return;
        }
        if (player.isFullInventory()) 
        {
           player.sendMessage(MessageCreator.createServerAlertMessage("Hành trang đầy.", ""));
           return;
        }
        
        // 1) Kiểm tra đủ nguyên liệu với đúng SỐ LƯỢNG theo amountMaterial
        for (short matId : player.thanKhiCraftState.getRandomMaterials()) 
        {
            int offset = player.thanKhiCraftState.getMaterialLevel() - 1; // vì đã cộng i ở lúc chọn
            int baseIdx = -1;
            for (int j = 0; j < startIDMaterial.length; j++) 
            { 
                if ((short) (startIDMaterial[j] + offset) == matId) 
                {
                    baseIdx = j;
                    break;
                }
            }
            if (baseIdx < 0) 
            {  
                player.sendMessage(MessageCreator.createServerAlertMessage("Dữ liệu nguyên liệu không hợp lệ, vui lòng thử lại", ""));
                return;
            }
            
            int required = amountMaterial[baseIdx];
           
            int materialMode = player.thanKhiCraftState.getMaterialMode();
            int lockHave = player.listGemitemLock[matId];
            int normalHave = player.listGemitem[matId];

            switch (materialMode) 
            {
                case 0: // chỉ dùng thường
                    if (normalHave < required) 
                    {
                        player.sendMessage(MessageCreator.createServerAlertMessage("Ngươi không đủ nguyên liệu: cần " + required + " " + Map.gemTemplate[matId].name, ""));
                        return;
                    }
                    // đủ -> trừ thường
                    player.listGemitem[matId] = normalHave - required;
                    break;
                case 1: // chỉ dùng khóa
                    if (lockHave < required) 
                    {
                        player.sendMessage(MessageCreator.createServerAlertMessage( "Ngươi không đủ nguyên liệu: cần " + required + " " + Map.gemTemplate[matId].name + " (Khóa)", ""));
                        return;
                    }
                    // đủ -> trừ khóa
                    player.listGemitemLock[matId] = lockHave - required;
                    break;

                case 2: // hỗn hợp, ưu tiên khóa
                    int totalHave = lockHave + normalHave;
                    if (totalHave < required) 
                    {
                        player.sendMessage(MessageCreator.createServerAlertMessage("Ngươi không đủ nguyên liệu: cần " + required + " " + Map.gemTemplate[matId].name + " (ưu tiên dùng Khóa)", ""));
                        return;
                    }
                    // trừ ưu tiên khóa trước, thiếu mới trừ thường
                    int useLock = Math.min(lockHave, required);
                    int remain = required - useLock;
                    player.listGemitemLock[matId] = lockHave - useLock;
                    if (remain > 0) 
                    {
                        player.listGemitem[matId] = normalHave - remain;
                    }
                break;
            }
            player.listGemitem[matId] = normalHave - required;
            break;
        }

        
        // 2) Trừ nguyên liệu theo đúng SỐ LƯỢNG
        for (short matId : player.thanKhiCraftState.getRandomMaterials()) 
        {
            int offset = player.thanKhiCraftState.getMaterialLevel() - 1;
            int baseIdx = -1;
            for (int j = 0; j < startIDMaterial.length; j++) 
            {
                if ((short) (startIDMaterial[j] + offset) == matId) 
                {
                    baseIdx = j;
                    break;
                }
            }
            int required = amountMaterial[baseIdx];
            
            if (player.listGemitemLock[matId] >= required) 
            {
                player.delGem(matId, required, player.thanKhiCraftState.getMaterialMode() != 0);
            } 
        }
        
        short[] idthankhi = new short[] { 890, 891, 892 };
        short iditem = idthankhi[player.thanKhiCraftState.getItemType()];
        ItemTemplates template = (ItemTemplates) ((Hashtable) itemTemplates.get(player.charClass)).get(new Integer(iditem));
        Item newItem = new Item(template, false, player.charClass, player.charClass, template.id);
        newItem.id = player.getIDItem();
        newItem.owner = player.charDBID;
        newItem.level = 50;
        newItem.lock = 0;
        newItem.identify = player.charDBID;
        newItem.clazz = (byte) -1;
        newItem.mDurable = 0;
        newItem.charSeal = player.charname;
        newItem.lock = (byte) (player.thanKhiCraftState.getMaterialMode() > 0 ? 1 : 0);

        int color = 0;
        int chance = Map.random(100); // 0..99
        switch (player.thanKhiCraftState.getMaterialLevel()) 
        {
            case 1:  color = 0;                     break;  // trắng
            case 2:  color = (chance < 20) ? 3 : 0; break;  // 20% xanh dương
            case 3:  color = (chance < 25) ? 2 : 3; break;  // 25% đỏ
            case 4:  color = (chance < 30) ? 1 : 2; break;  // 30% xanh lá
            case 5:  color = (chance < 35) ? 4 : 1; break;  // 35% tím
            case 6:  color = 4;                     break;  // luôn tím
            default: color = 0;                     break;
        }
        newItem.colorName = (byte) color;

        int hiddenCount = 0;
        switch (color) 
        {
            case 0: hiddenCount = 0;                                                  break; // Bình thường
            case 3: hiddenCount = (2 + Map.random(4 - 2 + 1));                        break; // 2..4
            case 2: hiddenCount = (3 + Map.random(6 - 3 + 1));                        break; // 3..6
            case 1: hiddenCount = (4 + Map.random(8 - 4 + 1));                        break; // 4..8
            case 4: hiddenCount = (5 + Map.random(9 - 5 + 1));                        break; // 5..9
            default: hiddenCount = 0;                                                 break;
        }
        if (hiddenCount > 9) hiddenCount = 9;  // chặn trần
        newItem.otherAtt[168 - 61] = (short) hiddenCount; // số dòng

        // --- NGUYÊN LIỆU NÂNG CẤP (AddOption theo quy tắc 2-2-1) ---
        java.util.List<Integer> pickedIdx = new java.util.ArrayList<>(5);
        while (pickedIdx.size() < 2) 
        {
            int idx = Map.random(5); // 0..4
            if (!pickedIdx.contains(idx)) pickedIdx.add(idx);
        }
        while (pickedIdx.size() < 4) 
        {
            int idx = 5 + Map.random(5); // 5..9
            if (!pickedIdx.contains(idx)) pickedIdx.add(idx);
        }
        pickedIdx.add(10); // item cuối cùng

        for (int idx : pickedIdx) 
        {
            short optId = item_upgrade_material_ids[idx]; // id Option biểu diễn nguyên liệu nâng cấp
            int qty = amountMaterial[idx];                // số lượng: 2 cho 10 item đầu, 1 cho item cuối
            newItem.otherAtt[optId - 61] = (short) qty;
        }

        int attrIdx = Map.random(item_attribute_ids.length);
        short attrOptId = item_attribute_ids[attrIdx];
        int attrValue = 100 + Map.random(81); // 100..180
        newItem.otherAtt[attrOptId - 61] = (short) attrValue;
        player.iItems.add(newItem);
        player.sendMessage(MessageCreator.createCharInventoryMessage(player, 1));
        player.sendMessage(MessageCreator.createServerAlertMessage("Chúc mừng! Ngươi đã tạo thành công " + template.name, ""));
    }
    
    
    public static void UpgradeThanKhi(Char player, short selectedItemIndex) 
    {
        final short GEM_ID_HOA_THACH_CAP1 = 258; // +1% tỉ lệ
        final short GEM_ID_HOA_THACH_CAP2 = 259; // +10% tỉ lệ
        ThanKhiCraftState st = player.thanKhiCraftState;
        if (st.getUpgradeSelection() < 0 || st.getUpgradeSelection() > 8)
        {
            player.sendMessage(MessageCreator.createServerAlertMessage("Vui lòng chọn chế độ nâng cấp ở NPC trước.", ""));
            return;
        }

        // 2) Lấy item cần nâng cấp theo danh sách đã build ở menu 61
        short[] ids = st.getUpgradeItemIds();
        if (ids == null || selectedItemIndex < 0 || selectedItemIndex >= ids.length)
        {
            player.sendMessage(MessageCreator.createServerAlertMessage("Không tìm thấy Thần Khí đã chọn trong hành trang.", ""));
            return;
        }
        short targetItemId = ids[selectedItemIndex];

        Item item = null;
        for (int i = 0; i < player.iItems.size(); i++)
        {
            Item it = (Item) player.iItems.get(i);
            if (it != null && it.id == targetItemId)
            {
                item = it;
                break;
            }
        }
        if (item == null)
        {
            player.sendMessage(MessageCreator.createServerAlertMessage("Không tìm thấy Thần Khí đã chọn trong hành trang.", ""));
            return;
        }

        // 3) Chỉ cho phép 3 loại Thần Khí (template id
        boolean isThanKhi = (item.tempateID == 890 || item.tempateID == 891 || item.tempateID == 892);
        if (!isThanKhi)
        {
            player.sendMessage(MessageCreator.createServerAlertMessage("Vật phẩm này không phải Thần Khí.", ""));
            return;
        }

        // 4) Xác định chế độ nguyên liệu & loại booster (Hỏa thạch)
        int upgradeSel   = st.getUpgradeSelection();         // 0..8
        int materialMode = (upgradeSel % 3);                 // 0: thường | 1: khóa     | 2: hỗn hợp
        int boosterType  = (upgradeSel / 3);                 // 0: thường | 1: HT1(+1%) | 2: HT2(+10%)
        int boosterPercent = (boosterType == 1 ? 1 : (boosterType == 2 ? 10 : 0));
        short boosterGemId = (boosterType == 1 ? GEM_ID_HOA_THACH_CAP1 : (boosterType == 2 ? GEM_ID_HOA_THACH_CAP2 : (short)0));

        // 5) Không thể vượt quá +10
        int curPlus;
        curPlus = item.plus;
        if (curPlus >= 10)
        {
            player.sendMessage(MessageCreator.createServerAlertMessage("Thần Khí đã đạt +10, không thể nâng cấp thêm.", ""));
            return;
        }

        //  Bảng tỉ lệ cơ bản (targetPlus = curPlus+1 => index 0..9)
        final int[] BASE_UPGRADE_RATE_BY_TARGET_PLUS = new int[] { 90, 80, 70, 60, 50, 40, 30, 25, 20, 15 };
        int targetPlus = curPlus + 1;
        int baseChance = (targetPlus >= 1 && targetPlus <= 10) ? BASE_UPGRADE_RATE_BY_TARGET_PLUS[targetPlus - 1] : 50;

        int finalChance = baseChance + boosterPercent;
        if (finalChance > 100) finalChance = 100;
        if (finalChance < 1)   finalChance = 1;

        //   Gom nguyên liệu cần tiêu (từ các option 169..179 đã "khắc" vào item) => đối chiếu startIDMaterial (cấp 6: +5 offset) & cộng dồn số lượng
        java.util.Map<Short, Integer> matsToConsume = new java.util.HashMap<>();
        java.util.HashSet<Short> optIdsSet = new java.util.HashSet<>();
        for (short x : item_upgrade_material_ids) optIdsSet.add(x);

        // otherAtt lưu theo [optId - 61]
        for (short optId = item_upgrade_material_ids[0]; optId <= item_upgrade_material_ids[item_upgrade_material_ids.length - 1]; optId++)
        {
            int idxStore = optId - 61;
            if (idxStore >= 0 && idxStore < item.otherAtt.length)
            {
                int qty = item.otherAtt[idxStore];
                if (qty > 0 && optIdsSet.contains(optId))
                {
                    int idxBase = optId - item_upgrade_material_ids[0]; // 0..10
                    if (idxBase >= 0 && idxBase < startIDMaterial.length)
                    {
                        short matIdLevel6 = (short) (startIDMaterial[idxBase] + 5); // cấp 6
                        matsToConsume.put(matIdLevel6, matsToConsume.getOrDefault(matIdLevel6, 0) + qty);
                    }
                }
            }
        }

        if (boosterGemId > 0) 
        {   
            matsToConsume.put(boosterGemId, matsToConsume.getOrDefault(boosterGemId, 0) + 1);
        }
        
        for (java.util.Map.Entry<Short, Integer> e : matsToConsume.entrySet())
        {
            short matId = e.getKey();
            int qtyNeed = e.getValue();

            int normalHave = (matId >= 0 && matId < player.listGemitem.length) ? player.listGemitem[matId] : 0;
            int lockHave   = (matId >= 0 && matId < player.listGemitemLock.length) ? player.listGemitemLock[matId] : 0;

            boolean ok;
            if (materialMode == 0)       ok = (normalHave >= qtyNeed);
            else if (materialMode == 1)  ok = (lockHave   >= qtyNeed);
            else                         ok = (normalHave + lockHave >= qtyNeed);

            if (!ok)
            { 
                player.sendMessage(MessageCreator.createServerAlertMessage("Thiếu nguyên liệu: cần " + qtyNeed + " " + Map.gemTemplate[matId].name + " (cấp 6).", ""));
                return;
            }
        }

        //  TRỪ NGUYÊN LIỆU theo chế độ (ưu tiên khóa khi hỗn hợp)
        for (java.util.Map.Entry<Short, Integer> e : matsToConsume.entrySet())
        {
            short matId = e.getKey();
            int qtyNeed = e.getValue();

            int normalHave = (matId >= 0 && matId < player.listGemitem.length) ? player.listGemitem[matId] : 0;
            int lockHave   = (matId >= 0 && matId < player.listGemitemLock.length) ? player.listGemitemLock[matId] : 0;

            if (materialMode == 0)
            {
                player.listGemitem[matId] = Math.max(0, normalHave - qtyNeed);
            }
            else if (materialMode == 1)
            {
                player.listGemitemLock[matId] = Math.max(0, lockHave - qtyNeed);
            }
            else // hỗn hợp: ưu tiên trừ khóa
            {
                int useLock = Math.min(lockHave, qtyNeed);
                player.listGemitemLock[matId] = lockHave - useLock;

                int remain = qtyNeed - useLock;
                if (remain > 0)
                {
                    player.listGemitem[matId] = Math.max(0, normalHave - remain);
                }
            }
        }

        //  Roll kết quả
        // BUG CU: Map.random(10000) trong khi finalChance la PHAN TRAM (90..15)
        // -> ti le that chi bang 1/100 bang thiet ke (nang +7 chi 0,30% thay vi 30%).
        // Dung Map.r.nextInt truc tiep vi Map.random() tao new Random(currentTimeMillis)
        // moi lan goi, nhieu lan bam trong cung 1 mili giay se ra CUNG mot ket qua.
        int roll = Map.r.nextInt(100);
        boolean success = (roll < finalChance);


        if (success)
        {
            if (materialMode != 0 && item.lock == 0) item.lock = 1; // Nếu dùng NL khóa/hỗn hợp => item bị khóa (nếu đang mở)
            item.plus = (byte) (curPlus + 1); 

            int newPlus = item.plus;

            // Cập nhật giá trị các dòng ẩn theo bậc + (tier = clamp(newPlus-1, 0..9))
            int tier = Math.max(0, Math.min(9, newPlus - 1));

            // Số dòng ẩn tối đa (đã lưu ở optId=168)
            int maxHidden = 0;
            int idxHiddenCount = 168 - 61;
            maxHidden = item.otherAtt[idxHiddenCount];

            // Dem SO DONG AN da mo. Vong lap ben duoi chi duyet HIDDEN_STAT_IDS,
            // khong bao gom dong sat thuong he (163..167) — theo dac ta thi dong he
            // la dong mac dinh, TACH RIENG khoi so dong an.
            // Ban cu khoi tao = 1 nen dong he bi tinh lan vao quota, lam mon 9 dong
            // chi mo duoc 8 dong an.
            int openedCount = 0;
            for (short statId : HIDDEN_STAT_IDS)
            {
                if (statId < 0) continue;
                int store = statId - 61;
                if (store < item.otherAtt.length && item.otherAtt[store] > 0) openedCount++;
            }
            
            // Cập nhật lại tham số cho các dòng ẩn đã có theo bảng HIDDEN_STAT_TABLE
            for (int si = 0; si < HIDDEN_STAT_IDS.length; si++)
            {
                short statOptId = HIDDEN_STAT_IDS[si];
                if (statOptId < 0) continue;
                int store = statOptId - 61;
                if (store < 0 || store >= item.otherAtt.length) continue;
                if (item.otherAtt[store] > 0)
                {
                    double v = HIDDEN_STAT_TABLE[si][tier];
                    int newParam = (int) Math.round(v);
                    item.otherAtt[store] = (short) newParam;
                }
            }

            // Tăng dần chỉ số thuộc tính hệ (163..167): +4 mỗi lần nâng cấp thành công
            java.util.HashSet<Short> attrSet = new java.util.HashSet<>();
            for (short id : item_attribute_ids) attrSet.add(id);
            for (short attrId : item_attribute_ids)
            {
                int store = attrId - 61;
                if (store >= 0 && store < item.otherAtt.length && item.otherAtt[store] > 0)
                {
                    int val = item.otherAtt[store] + 4;
                    item.otherAtt[store] = (short) val;
                }
            }

            // Tính số dòng ẩn cần mở thêm: +2/+4/+6/+8 mở thêm 1; +10 mở đến tối đa
            int needOpen = 0;
            if (newPlus == 2 || newPlus == 4 || newPlus == 6 || newPlus == 8) needOpen = 1;
            else if (newPlus == 10) 
            {
                needOpen = Math.max(0, maxHidden - openedCount); 
            }

            java.util.HashSet<Short> existIds = new java.util.HashSet<>();
            for (short statId : HIDDEN_STAT_IDS)
            {
                if (statId < 0) continue;
                int s = statId - 61;
                if (s >= 0 && s < item.otherAtt.length && item.otherAtt[s] > 0) existIds.add(statId);
            }

            int safety = 0;
            while (needOpen > 0 && openedCount < maxHidden)
            {
                safety++;
                int idx = Map.random(HIDDEN_STAT_IDS.length);
                short statOptId = HIDDEN_STAT_IDS[idx];
                if (statOptId < 0) continue;
                if (existIds.contains(statOptId)) continue;

                double v = HIDDEN_STAT_TABLE[idx][tier];
                int param = (Math.abs(v - Math.rint(v)) < 1e-9) ? (int) Math.round(v) : (int) Math.round(v * 10.0);

                int store = statOptId - 61;
                if (store >= 0 && store < item.otherAtt.length)
                {
                    item.otherAtt[store] = (short) param;
                    existIds.add(statOptId);
                    openedCount++;
                    needOpen--;
                }
            }

            // Đổi nguyên liệu ở mốc +3/+6/+9
            if (newPlus == 3 || newPlus == 6 || newPlus == 9)
            {
                // Xóa các option cũ 169..179
                for (short optId = item_upgrade_material_ids[0]; optId <= item_upgrade_material_ids[item_upgrade_material_ids.length - 1]; optId++)
                {
                    int store = optId - 61;
                    if (store >= 0 && store < item.otherAtt.length) item.otherAtt[store] = 0;
                }
                // Tính số mốc hiện tại
                int milestone = newPlus / 3; // 3->1, 6->2, 9->3

                // Lấy 5 nguyên liệu (2-2-1) mới
                int[] pickedIdx = RandomBaseMaterialIndices();
                for (int idx : pickedIdx)
                {
                    short optId = item_upgrade_material_ids[idx];
                    int baseQty = amountMaterial[idx]; // 0..9: 2 | 10: 1
                    int qty = (idx == 10) ? (baseQty + milestone * 1) : (baseQty + milestone * 2);
                    int store = optId - 61;
                    if (store >= 0 && store < item.otherAtt.length) item.otherAtt[store] = (short) qty;
                }
            }

            // Dat +10 la het duong nang cap (dau ham da chan curPlus >= 10),
            // nen xoa het cac dong nguyen lieu 169..179 cho khoi rac bang chi so.
            if (newPlus >= 10)
            {
                for (short optId = item_upgrade_material_ids[0]; optId <= item_upgrade_material_ids[item_upgrade_material_ids.length - 1]; optId++)
                {
                    int store = optId - 61;
                    if (store >= 0 && store < item.otherAtt.length) item.otherAtt[store] = 0;
                }
            }

            player.sendMessage(MessageCreator.createServerAlertMessage("Nâng cấp thành công! Thần Khí đạt +" + newPlus, ""));
            player.sendMessage(MessageCreator.createCharInventoryMessage(player, 1)); // refresh
        }
        else
        {
            player.sendMessage(MessageCreator.createServerAlertMessage("Nâng cấp thất bại (tỉ lệ " + finalChance + "%)", ""));
            player.sendMessage(MessageCreator.createCharInventoryMessage(player, 1)); // refresh
        }
    }
    
    public static void RerollThanKhiAttribute(Char player, short selectedItemIndex) 
    { 
        if (player.thanKhiCraftState.getUpgradeItemIds() == null || player.thanKhiCraftState.getUpgradeItemIds().length == 0)
        {
            player.sendMessage(MessageCreator.createServerAlertMessage("Trạng thái đổi dòng không hợp lệ. Hãy mở NPC và chọn lại Thần Khí.", ""));
            return;
        }
        if (selectedItemIndex < 0 || selectedItemIndex >= player.thanKhiCraftState.getUpgradeItemIds().length)
        {
            player.sendMessage(MessageCreator.createServerAlertMessage("Lựa chọn vật phẩm không hợp lệ.", ""));
            return;
        }
        
        ThanKhiCraftState st = player.thanKhiCraftState;
        short[] ids = st.getUpgradeItemIds();
        if (ids == null || selectedItemIndex < 0 || selectedItemIndex >= ids.length)
        {
            player.sendMessage(MessageCreator.createServerAlertMessage("Không tìm thấy Thần Khí đã chọn trong hành trang.", ""));
            return;
        }
        short targetItemId = ids[selectedItemIndex];

        Item item = null;
        for (int i = 0; i < player.iItems.size(); i++)
        {
            Item it = (Item) player.iItems.get(i);
            if (it != null && it.id == targetItemId)
            {
                item = it;
                break;
            }
        }
        if (item == null)
        {
            player.sendMessage(MessageCreator.createServerAlertMessage("Không tìm thấy Thần Khí đã chọn trong hành trang.", ""));
            return;
        }
        
        boolean isThanKhi = (item.tempateID == 890 || item.tempateID == 891 || item.tempateID == 892);
        if (!isThanKhi)
        {
            player.sendMessage(MessageCreator.createServerAlertMessage("Vật phẩm này không phải Thần Khí.", ""));
            return;
        }
        
        final short GEM_ID_HOA_THACH_CAP2 = 259; // Cần 1 viên Hỏa thạch cấp 2
        int materialMode = (st.getMaterialMode() >= 0) ? st.getMaterialMode() : 2; // 0: thường | 1: khóa | 2: hỗn hợp

        // Kiểm tra đủ Hỏa thạch cấp 2 theo mode
        int normalHave = (GEM_ID_HOA_THACH_CAP2 >= 0 && GEM_ID_HOA_THACH_CAP2 < player.listGemitem.length) ? player.listGemitem[GEM_ID_HOA_THACH_CAP2] : 0;
        int lockHave   = (GEM_ID_HOA_THACH_CAP2 >= 0 && GEM_ID_HOA_THACH_CAP2 < player.listGemitemLock.length) ? player.listGemitemLock[GEM_ID_HOA_THACH_CAP2] : 0;

        boolean enough;
        if (materialMode == 0)       enough = (normalHave >= 1);
        else if (materialMode == 1)  enough = (lockHave   >= 1);
        else                         enough = (normalHave + lockHave >= 1);

        if (!enough)
        {
            player.sendMessage(MessageCreator.createServerAlertMessage("Thiếu nguyên liệu: cần 1 Hỏa thạch cấp 2.", ""));
            return;
        }

        // Tìm dòng hệ hiện có (163..167)
        short oldAttrId = -1;
        int oldStoreIdx = -1;
        for (short attrId : item_attribute_ids)
        {
            int store = attrId - 61;
            if (store >= 0 && store < item.otherAtt.length && item.otherAtt[store] > 0)
            {
                oldAttrId = attrId;
                oldStoreIdx = store;
                break;
            }
        }
        if (oldAttrId == -1)
        {
            player.sendMessage(MessageCreator.createServerAlertMessage("Thần Khí không có dòng hệ để đổi.", ""));
            return;
        }

        // Trừ 1 viên HT cấp 2 theo mode (ưu tiên trừ khóa nếu hỗn hợp)
        if (materialMode == 0)
        {
            if (normalHave > 0) player.listGemitem[GEM_ID_HOA_THACH_CAP2] = normalHave - 1;
            else
            {
                player.sendMessage(MessageCreator.createServerAlertMessage("Thiếu nguyên liệu: cần 1 Hỏa thạch cấp 2.", ""));
                return;
            }
        }
        else if (materialMode == 1)
        {
            if (lockHave > 0) player.listGemitemLock[GEM_ID_HOA_THACH_CAP2] = lockHave - 1;
            else
            {
                player.sendMessage(MessageCreator.createServerAlertMessage("Thiếu nguyên liệu: cần 1 Hỏa thạch cấp 2 (Khóa).", ""));
                return;
            }
        }
        else
        {
            if (lockHave > 0)
            {
                player.listGemitemLock[GEM_ID_HOA_THACH_CAP2] = lockHave - 1;
            }
            else if (normalHave > 0)
            {
                player.listGemitem[GEM_ID_HOA_THACH_CAP2] = normalHave - 1;
            }
            else
            {
                player.sendMessage(MessageCreator.createServerAlertMessage("Thiếu nguyên liệu: cần 1 Hỏa thạch cấp 2.", ""));
                return;
            }
        }

        // Random lại hệ (có thể trùng)
        int rndIdx = Map.random(item_attribute_ids.length); // 0..4
        short newAttrId = item_attribute_ids[rndIdx];
        int newStoreIdx = newAttrId - 61;
        int attrValue = 170 + Map.random(50); // 170..219

        // Xóa dòng hệ cũ và gán dòng hệ mới (ghi đè param)
        if (oldStoreIdx >= 0 && oldStoreIdx < item.otherAtt.length) item.otherAtt[oldStoreIdx] = 0;
        if (newStoreIdx >= 0 && newStoreIdx < item.otherAtt.length) item.otherAtt[newStoreIdx] = (short) attrValue;

        // Khóa item nếu dùng NL khóa/hỗn hợp
        if (materialMode != 0 && item.lock == 0) item.lock = 1;

        // Thông báo
        int oldNameIdx = -1, newNameIdx = -1;
        for (int i = 0; i < item_attribute_ids.length; i++)
        {
            if (item_attribute_ids[i] == oldAttrId) oldNameIdx = i;
            if (item_attribute_ids[i] == newAttrId) newNameIdx = i;
        }

        player.sendMessage(MessageCreator.createServerAlertMessage("Đổi dòng hệ thành công: " + item_attribute_names[newNameIdx] + " : " + attrValue + "", ""));
        player.sendMessage(MessageCreator.createCharInventoryMessage(player, 1)); // refresh túi
    }
}
