package game.model;

import game.core.j2me.Graphics;
import game.render.GCanvas;
import game.render.Res;
import game.render.screen.CreateCharScr;
import game.render.screen.Font;
import game.render.screen.GameData;
import game.render.screen.WindowInfoScr;
import java.util.Vector;

public class ItemInInventory {
    public byte charClazz;
    public byte colorName;
    public int dayUse;
    public short durable;
    public short idTemplate;
    public short itemID;
    public short itemOldID;
    public long lastTime;
    public short mDurable;
    public byte maxNgoc;
    public String nameCharBet;
    public String nameCharSell;
    public byte numKham;
    public short number;
    public short plusTemplate;
    public byte potionType;
    public int prizeBet;
    public int prizeFix;
    public short[] property;
    public long timeEnd;
    public byte totalKham;
    public byte viTriVe;
    private static String[][] groupKick = {new String[]{"Quần", "Nhẫn dưới"}, new String[]{"Ngọc", "Găng"}, new String[]{"áo", "Dây chuyền"}, new String[]{"áo", "Dây chuyền"}, new String[]{"áo", "Dây chuyền"}, new String[]{"áo", "Dây chuyền"}, new String[]{"áo", "Dây chuyền"}, new String[]{"áo", "Dây chuyền"}, new String[]{"Nón", "Vũ khí", "Ngọc bội", "Găng"}, new String[]{"Nhẫn dưới", "Quần"}, new String[]{"Nón", "Vũ khí"}, new String[]{"Giày", "Nhẫn trên"}, new String[]{"Giày", "Nhẫn trên"}};
    private static String[][] kickHe = {new String[]{"Kim"}, new String[]{"Thủy"}, new String[]{"Mộc"}, new String[]{"Hỏa"}, new String[]{"Thổ"}};
    private static String[][] groupKickAnimal = {new String[]{"Nón"}, new String[]{"Bàn đạp"}, new String[]{"Hộ uyển"}, new String[]{"Yên cương"}, new String[]{"Giáp"}};
    public static final String[] attrName = {"Tấn công", "Thủ vật", "Né tránh", "Chính xác", "Chí mạng", "Sức khoẻ", "Thủ ma", "Chỉ số 7", "Chỉ số 8", "Chỉ số 9"};
    static String[] na = {"Giáp nhẹ", "Giáp nặng", "Giáp vải", "Giáp nặng", "Giáp nhẹ", ""};
    static String[] nameNguHanh = {"Giảm st vật: ", "Giảm st ma: ", "Tăng tc: ", "Bỏ qua pt: ", "Phản st: ", "Bỏ qua tc ma", "Bỏ qua tc vật"};
    static String[] nameHangItem = {"", "nhất phẩm", "nhị phẩm", "tam phẩm", "tứ phẩm", "ngũ phẩm"};
    static String[] he = {"hệ thuỷ", "hệ mộc", "hệ hoả", "hệ thổ", "hệ kim"};
    static String[] magic = {"ma pháp", "vật lý", ""};
    public static byte typeItemBuy = 2;
    private static int[] color = {7667443, 3334656, 16705024};
    public byte[] newAtb = new byte[10];
    public byte[] addMoreLevelSkill = new byte[15];
    public byte[] lockAtb = new byte[3];
    public String nameCharSeal = "";
    public byte catagory = 3;
    public byte isItemDrop = 0;
    public byte heItem = -1;
    public byte beKicked = 0;
    public byte hangItem = -1;
    public byte magic_physic = 2;
    public short[] itemAttribute = new short[10];
    public short level = -1;
    public boolean isSelling = false;
    public boolean isShopItem = false;
    public int prize = 0;
    public int lock = 0;
    public byte clazz = -1;
    public short[] attCreate = null;
    public boolean isEnoughData = false;
    public boolean isTrade = false;
    public Vector allAttribute = new Vector();
    boolean[][] attribShowedForType = {new boolean[]{false, true, true, true, false, false, true, false, false, false}, new boolean[]{false, true, true, true, false, false, true, false, false, false}, new boolean[]{false, true, true, true, false, false, true, false, false, false}, new boolean[]{true, false, false, true, true, false, false, false, false, false}, new boolean[]{true, false, false, true, true, false, false, false, false, false}, new boolean[]{true, false, false, true, true, false, false, false, false, false}, new boolean[]{true, false, false, true, true, false, false, false, false, false}, new boolean[]{true, false, false, true, true, false, false, false, false, false}, new boolean[]{true, false, false, true, false, false, false, false, false, false}, new boolean[]{true, false, false, false, true, false, false, false, false, false}, new boolean[]{false, true, true, false, false, false, true, false, false, false}, new boolean[]{false, true, false, true, true, false, true, false, false, false}, new boolean[]{false, false, false, true, true, true, false, false, false, false}, new boolean[]{true, false, false, false, false, false, false, false, false, false}, new boolean[]{false, true, false, false, false, false, true, false, false, false}, new boolean[]{true, false, false, true, false, false, false, false, false, false}, new boolean[]{false, true, false, false, false, false, true, false, false, false}, new boolean[]{true, false, false, false, true, false, false, false, false, false}, new boolean[]{false, true, false, false, false, false, true, false, false, false}, new boolean[]{false, true, false, false, false, false, true, false, false, false}};
    public short[] otherAtt = null;
    int count = Res.rnd(68);
    public String daySell = "";
    public byte kindItemInventory = 0;

    public static boolean isAnimalArmor(int i) {
        return i >= 14 && i <= 18;
    }

    public static boolean isArmor(int i) {
        return i < 3;
    }

    public static boolean isItemChar(int i) {
        return i < 13;
    }

    public static boolean isModelClothe(int i) {
        return i == 1;
    }

    public static boolean isThanThu(int i) {
        return i == 25;
    }

    public short[] getPoint() {
        return null;
    }

    public boolean isMyItem() {
        return this.nameCharSell.equals(GCanvas.gameScr.mainChar.name);
    }

    public ItemTemplate getTemplate() {
        return Res.getItem(this.charClazz, this.idTemplate);
    }

    public String[] getName() {
        String[] strArr = {"", "", ""};
        if (this.catagory == 3) {
            ItemTemplate item = Res.getItem(this.charClazz, this.idTemplate);
            String str = (this.plusTemplate > 0 ? " +" + ((int) this.plusTemplate) : "") + " " + magic[this.magic_physic];
            strArr[0] = ((int) this.colorName) + item.name + (this.plusTemplate > 0 ? " +" + ((int) this.plusTemplate) : "");
            strArr[1] = "Giá bán: " + this.prize + " xu";
            strArr[2] = "Giá bid: " + this.prizeBet + " xu";
        }
        return strArr;
    }

    public String compareItem(ItemInInventory itemInInventory) {
        String str;
        String str2;
        if (this.catagory != 3) {
            return "";
        }
        ItemTemplate item = Res.getItem(this.charClazz, this.idTemplate);
        if (!isModelClothe(item.attb[9])) {
            String str3 = (this.plusTemplate > 0 ? " +" + ((int) this.plusTemplate) : "") + " " + magic[this.magic_physic];
            if (this.hangItem > -1) {
                str3 = str3 + " " + nameHangItem[this.hangItem];
            }
            if (this.heItem > -1) {
                str3 = str3 + " " + he[this.heItem];
            }
            String str4 = "" + setTemp(item.name + str3, "" + ((int) this.colorName));
            if (!this.nameCharSeal.equals("")) {
                str4 = str4 + "\n0" + this.nameCharSeal.toUpperCase() + " tạo";
            }
            if (this.lock == 1) {
                str4 = str4 + "\nĐã khoá";
            }
            if (isArmor(item.type) && item.type != 13 && !isAnimalArmor(item.type)) {
                str4 = str4 + "\n" + na[this.clazz];
            }
            if (this.clazz != -1 && item.type != 13 && item.type != 19 && !isAnimalArmor(item.type)) {
                str4 = str4 + "\n" + Res.nameClazz[this.clazz];
            }
            if (item.level != -1) {
                str4 = str4 + "\nYêu cầu lv: " + ((int) this.level);
            }
            str = str4 + "\nĐộ bền: " + ((int) this.durable);
            for (byte b = 0; b < this.allAttribute.size(); b = (byte) (b + 1)) {
                InfoAttributeItem infoAttributeItem = (InfoAttributeItem) this.allAttribute.elementAt(b);
                str = str + setTemp(infoAttributeItem.getName(this.clazz) + infoAttributeItem.getValue() + (infoAttributeItem.isPercent() ? "%" : ""), ((int) infoAttributeItem.getColorPaint(this.beKicked == 1)) + "");
                for (int i = 0; i < itemInInventory.allAttribute.size(); i++) {
                    InfoAttributeItem infoAttributeItem2 = (InfoAttributeItem) this.allAttribute.elementAt(b);
                    if (infoAttributeItem2.getID() == infoAttributeItem.getID()) {
                        if (infoAttributeItem2.getValueAtt() > infoAttributeItem.getValueAtt()) {
                            str = str + "~1" + (infoAttributeItem2.getValueAtt() - infoAttributeItem.getValueAtt());
                        } else if (infoAttributeItem2.getValueAtt() > infoAttributeItem.getValueAtt()) {
                            str = str + "~0" + (infoAttributeItem.getValueAtt() - infoAttributeItem2.getValueAtt());
                        }
                    }
                }
            }
            try {
                byte b2 = this.hangItem;
                if (b2 > 0 && b2 < 5 && item.type != 13 && item.type < 13) {
                    if (item.type != 8) {
                        str2 = str + setTemp("cần " + groupKick[item.type][0] + " hoặc " + groupKick[item.type][1] + " hệ " + kickHe[this.heItem][0] + " để kích ngũ hành", "0");
                    } else if (this.viTriVe == 1) {
                        str2 = str + setTemp("Nhẫn trên cần " + groupKick[item.type][0] + " hoặc " + groupKick[item.type][1] + " hệ " + kickHe[this.heItem][0] + " để kích ngũ hành", "0");
                    } else {
                        str2 = str + setTemp("Nhẫn dưới cần " + groupKick[item.type][2] + " hoặc " + groupKick[item.type][3] + " hệ " + kickHe[this.heItem][0] + " để kích ngũ hành", "0");
                    }
                    str = str2;
                }
                byte b3 = this.hangItem;
                if (b3 > 0 && b3 < 5 && isAnimalArmor(item.type)) {
                    str = str + setTemp("cần " + groupKickAnimal[item.type - 14][0] + " hệ " + kickHe[this.heItem][0] + " để kích ngũ hành", "0");
                }
            } catch (Exception unused) {
            }
        } else {
            str = (((("" + item.name) + "\nTấn công tăng: " + ((int) item.attb[0]) + "%") + "\nThủ tăng: " + ((int) item.attb[1]) + "%") + "\nHP tăng: " + ((int) item.attb[2])) + "\nMP tăng: " + ((int) item.attb[3]);
        }
        int i2 = this.dayUse;
        if (i2 != 0) {
            long jCurrentTimeMillis = ((long) i2) - ((System.currentTimeMillis() - this.lastTime) / 60000);
            if (jCurrentTimeMillis < 0) {
                jCurrentTimeMillis = 1;
            }
            long j = jCurrentTimeMillis / 60;
            long j2 = jCurrentTimeMillis % 60;
            str = str + setTemp("\nThời gian còn lại: " + (j > 0 ? j + "h " : "") + (j2 > 0 ? j2 + "p" : ""), "0");
        }
        if (this.timeEnd > 0) {
            return ((((str + setTemp(this.nameCharSell, "0")) + setTemp(this.daySell, "0")) + setTemp("Bid: " + this.prizeBet + " xu", "0")) + setTemp(this.nameCharBet.equals("") ? "----" : this.nameCharBet + " bid", "0")) + setTemp(getTimeSell(), "0");
        }
        return str;
    }

    public String getDescription(boolean z) {
        String str;
        String str2;
        if (this.catagory != 3) {
            return "";
        }
        ItemTemplate item = Res.getItem(this.charClazz, this.idTemplate);
        if (!isModelClothe(item.attb[9])) {
            String str3 = (this.plusTemplate > 0 ? " +" + ((int) this.plusTemplate) : "") + " " + magic[this.magic_physic];
            if (this.hangItem > -1) {
                str3 = str3 + " " + nameHangItem[this.hangItem];
            }
            if (this.heItem > -1) {
                str3 = str3 + " " + he[this.heItem];
            }
            String str4 = "" + setTemp(item.name + str3, "" + ((int) this.colorName));
            if (!this.nameCharSeal.equals("")) {
                str4 = str4 + "\n0" + this.nameCharSeal.toLowerCase() + (this.nameCharSeal.indexOf("-") > -1 ? "" : " tạo");
            }
            if (this.lock == 1) {
                str4 = str4 + "\nĐã khoá";
            }
            if (isArmor(item.type) && item.type != 13 && !isAnimalArmor(item.type)) {
                str4 = str4 + "\n" + na[this.clazz];
            }
            if (this.clazz != -1 && item.type != 13 && item.type != 19 && !isAnimalArmor(item.type)) {
                str4 = str4 + "\n" + Res.nameClazz[this.clazz];
            }
            if (this.isSelling) {
                str4 = str4 + "\nGiá bán: " + GCanvas.getMoneys(this.prize) + "~";
            } else if (z) {
                str4 = str4 + "\nGiá bán lại: " + GCanvas.getMoneys((item.price / 5) + (this.plusTemplate * 1000)) + "~";
            }
            if (item.level != -1) {
                str4 = str4 + "\nYêu cầu lv: " + ((int) this.level);
            }
            str = str4 + "\nĐộ bền: " + ((int) this.durable);
            for (byte b = 0; b < this.allAttribute.size(); b = (byte) (b + 1)) {
                InfoAttributeItem infoAttributeItem = (InfoAttributeItem) this.allAttribute.elementAt(b);
                str = str + setTemp(infoAttributeItem.getName(this.clazz) + infoAttributeItem.getValue() + (infoAttributeItem.isPercent() ? "%" : ""), ((int) infoAttributeItem.getColorPaint(this.beKicked == 1)) + "");
            }
            try {
                byte b2 = this.hangItem;
                if (b2 > 0 && b2 < 5 && item.type != 13 && item.type < 13) {
                    if (item.type != 8) {
                        str2 = str + setTemp("cần " + groupKick[item.type][0] + " hoặc " + groupKick[item.type][1] + " hệ " + kickHe[this.heItem][0] + " để kích ngũ hành", "0");
                    } else if (this.viTriVe == 1) {
                        str2 = str + setTemp("Nhẫn trên cần " + groupKick[item.type][0] + " hoặc " + groupKick[item.type][1] + " hệ " + kickHe[this.heItem][0] + " để kích ngũ hành", "0");
                    } else {
                        str2 = str + setTemp("Nhẫn dưới cần " + groupKick[item.type][2] + " hoặc " + groupKick[item.type][3] + " hệ " + kickHe[this.heItem][0] + " để kích ngũ hành", "0");
                    }
                    str = str2;
                }
                byte b3 = this.hangItem;
                if (b3 > 0 && b3 < 5 && isAnimalArmor(item.type)) {
                    str = str + setTemp("cần " + groupKickAnimal[item.type - 14][0] + " hệ " + kickHe[this.heItem][0] + " để kích ngũ hành", "0");
                }
            } catch (Exception unused) {
            }
        } else {
            String str5 = "" + item.name;
            if (!this.nameCharSeal.equals("")) {
                str5 = str5 + "\n0" + this.nameCharSeal.toLowerCase() + (this.nameCharSeal.indexOf("-") > -1 ? "" : " tạo");
            }
            str = str5;
            for (byte b4 = 0; b4 < this.allAttribute.size(); b4 = (byte) (b4 + 1)) {
                InfoAttributeItem infoAttributeItem2 = (InfoAttributeItem) this.allAttribute.elementAt(b4);
                str = str + setTemp(infoAttributeItem2.getName(this.clazz) + infoAttributeItem2.getValue() + (infoAttributeItem2.isPercent() ? "%" : ""), ((int) infoAttributeItem2.getColorPaint(this.beKicked == 1)) + "");
            }
        }
        int i = this.dayUse;
        if (i != 0) {
            long jCurrentTimeMillis = ((long) i) - ((System.currentTimeMillis() - this.lastTime) / 60000);
            if (jCurrentTimeMillis < 0) {
                jCurrentTimeMillis = 1;
            }
            long j = jCurrentTimeMillis / 60;
            long j2 = jCurrentTimeMillis % 60;
            str = str + setTemp("\nThời gian còn lại: " + (j > 0 ? j + "h " : "") + (j2 > 0 ? j2 + "p" : ""), "0");
        }
        if (this.timeEnd > 0) {
            return ((((str + setTemp(this.nameCharSell, "0")) + setTemp(this.daySell, "0")) + setTemp("Bid: " + this.prizeBet + " xu", "0")) + setTemp(this.nameCharBet.equals("") ? "----" : this.nameCharBet + " bid", "0")) + setTemp(getTimeSell(), "0");
        }
        return str;
    }

    public static String setTemp(String str, String str2) {
        String str3 = "";
        for (String str4 : Font.normalFontColor1.splitFontBStrInLine(str, 110)) {
            str3 = str3 + "\n" + str2 + str4.trim();
        }
        return str3;
    }

    public String getTemplateDescription() {
        String s = " ";
        try {
            String[] na2 = {"Giáp nhẹ", "Giáp nặng", "Giáp vải", "Giáp nặng", "Giáp nhẹ"};
            if (this.catagory == 3) {
                ItemTemplate item = Res.getItem(this.charClazz, this.idTemplate);
                if (this.idTemplate < 264 || this.idTemplate > 267) {
                    String plus = this.plusTemplate > 0 ? "+" + ((int) this.plusTemplate) : "";
                    s = " " + item.name;
                    s = s + " " + plus;
                    if ((item.id < 79 || item.id > 137) && item.type != 13 && !isAnimalArmor(item.type)) {
                        s = s + "\n " + na2[this.clazz];
                    }
                    if (item.id > 113 && item.id <= 137 && item.type != 13) {
                        s = s + "\n " + Res.nameClazz[this.clazz];
                    }
                    if (item.ndayLoan == 0) {
                        s = s + "\n Giá: " + GCanvas.getMoneys(item.price + (this.plusTemplate * 1000)) + " xu";
                    } else {
                        s = s + "\n Giá: " + GCanvas.getMoneys(item.price + this.plusTemplate) + " Lượng";
                    }
                    if (this.level != -1) {
                        s = s + "\n Yêu cầu lv: " + ((int) this.level);
                    }
                    s = s + "\n Độ bền: " + ((int) this.durable);
                    if (item.clazz != -1 && item.type != 13) {
                        s = s + "\n Dành cho: " + CreateCharScr.clazz[this.clazz][CreateCharScr.selectGender].name;
                    }
                    int[] attb = Res.getAttributeItem(this.charClazz, this.idTemplate, item.type);
                    if (item.type < 2 || item.type == 10 || item.type == 11 || isAnimalArmor(item.type)) {
                        if (typeItemBuy == 0) {
                            int v = attb[1];
                            attb[6] = v;
                            attb[1] = v / 2;
                        } else {
                            attb[6] = attb[1] >> 1;
                        }
                    }
                    for (int i = 0; i < 10; i++) {
                        if (this.attribShowedForType[item.type][i]) {
                            s = s + "\n " + attrName[i] + ": " + attb[i];
                        }
                    }
                } else {
                    s = " " + item.name;
                    s = s + "\n Tấn công tăng: " + ((int) item.attb[0]) + "%";
                    s = s + "\n Thủ tăng: " + ((int) item.attb[1]) + "%";
                    s = s + "\n HP tăng: " + ((int) item.attb[2]);
                    s = s + "\n MP tăng: " + ((int) item.attb[3]);
                }
                if (item.ndayLoan > 0) {
                    s = s + setTemp("\nThời hạn: " + ((int) item.ndayLoan) + " Phút.", "0");
                }
            }
            if (this.catagory == 4 && this.isShopItem) {
                s = "" + MainChar.listPotion[this.potionType].name;
                s = s + "\n Giá: " + GCanvas.getMoneys(Res.potionTemplates[this.potionType].price) + "$";
                if (Res.potionTemplates[this.potionType].recovered >= 80) {
                    s = s + "\n Tăng " + ((int) Res.potionTemplates[this.potionType].recovered) + " " + MainChar.listPotion[this.potionType].name2;
                } else if (Res.potionTemplates[this.potionType].recovered == 0) {
                    s = s + "\n Khăn đeo dùng khi đánh nhau.";
                } else if (Res.potionTemplates[this.potionType].recovered == -1) {
                    s = s + "\n Dịch chuyển tức thời về làng.";
                }
                int n1 = GCanvas.gameScr.mainChar.potions[this.potionType];
                int n2 = 0;
                int size = WindowInfoScr.gI().readyBuy.size();
                for (int i2 = 0; i2 < size; i2++) {
                    ItemInInventory item2 = (ItemInInventory) WindowInfoScr.gI().readyBuy.elementAt(i2);
                    if (item2.potionType == this.potionType) {
                        n2 = item2.number;
                    }
                }
                if (n2 == 0) {
                    s = s + "\n Đang có: " + n1 + " cái.";
                } else {
                    s = s + "\n Đang có: " + n1 + "+" + n2 + " cái.";
                }
                if (n1 + n2 >= 999) {
                    s = s + "\n (tối đa)";
                }
            }
        } catch (Exception e) {
            Cout.println("LOI ROI ");
        }
        return s;
    }

    public String getInfoItemUserSell() {
        ItemTemplate item = Res.getItem(this.charClazz, this.idTemplate);
        String str = (this.plusTemplate > 0 ? " +" + ((int) this.plusTemplate) : "") + " " + magic[this.magic_physic];
        if (this.hangItem > -1) {
            str = str + " " + nameHangItem[this.hangItem];
        }
        if (this.heItem > -1) {
            str = str + " " + he[this.heItem];
        }
        String str2 = "" + setTemp(item.name + str, "" + ((int) this.colorName));
        if (!this.nameCharSeal.equals("")) {
            str2 = str2 + "\n0" + this.nameCharSeal.toUpperCase() + " tạo";
        }
        if (this.level != -1) {
            str2 = str2 + "\nYêu cầu lv: " + ((int) this.level);
        }
        String str3 = str2 + "\nĐộ bền: " + ((int) this.durable);
        if (this.clazz != -1) {
            str3 = str3 + "\nDòng: " + CreateCharScr.clazz[this.clazz][CreateCharScr.selectGender].name;
        }
        for (byte b = 0; b < this.allAttribute.size(); b = (byte) (b + 1)) {
            InfoAttributeItem infoAttributeItem = (InfoAttributeItem) this.allAttribute.elementAt(b);
            str3 = str3 + setTemp(infoAttributeItem.getName(this.clazz) + infoAttributeItem.getValue() + (infoAttributeItem.isPercent() ? "%" : ""), ((int) infoAttributeItem.getColorPaint(false)) + "");
        }
        if (this.prize <= 0) {
            return str3;
        }
        String str4 = str3 + "\nGiá bán:\n0" + GCanvas.getMoneys(this.prize);
        Cout.println(this.prize + " GIA BAN " + GCanvas.getMoneys(this.prize));
        return str4;
    }

    public static Vector getAllItemChar() {
        Vector vector = new Vector();
        for (int i = 0; i < Char.inventory.size(); i++) {
            ItemInInventory itemInInventory = (ItemInInventory) Char.inventory.elementAt(i);
            if (isItemChar(Res.getItem(itemInInventory.charClazz, itemInInventory.idTemplate).type) && itemInInventory.dayUse == 0) {
                vector.addElement(itemInInventory);
            }
        }
        return vector;
    }

    public static Vector getAllItem(int i) {
        Vector vector = new Vector();
        for (int i2 = 0; i2 < Char.inventory.size(); i2++) {
            ItemInInventory itemInInventory = (ItemInInventory) Char.inventory.elementAt(i2);
            if (itemInInventory.colorName == i) {
                vector.addElement(itemInInventory);
            }
        }
        return vector;
    }

    public static Vector getAllItemCharExcepColor(int i) {
        Vector vector = new Vector();
        for (int i2 = 0; i2 < Char.inventory.size(); i2++) {
            ItemInInventory itemInInventory = (ItemInInventory) Char.inventory.elementAt(i2);
            if (itemInInventory.colorName != i && isItemChar(itemInInventory.getTemplate().type)) {
                vector.addElement(itemInInventory);
            }
        }
        return vector;
    }

    public static Vector getAllItemAnimal(int i, int i2) {
        Vector vector = new Vector();
        for (int i3 = 0; i3 < Char.inventory.size(); i3++) {
            ItemInInventory itemInInventory = (ItemInInventory) Char.inventory.elementAt(i3);
            if (itemInInventory.colorName == i && itemInInventory.hangItem == i2 && isAnimalArmor(itemInInventory.getTemplate().type)) {
                vector.addElement(itemInInventory);
            }
        }
        return vector;
    }

    public static Vector getAllItemCanSell() {
        Vector vector = new Vector();
        for (int i = 0; i < Char.inventory.size(); i++) {
            ItemInInventory itemInInventory = (ItemInInventory) Char.inventory.elementAt(i);
            if (!isModelClothe(Res.getItem(itemInInventory.charClazz, itemInInventory.idTemplate).attb[9]) && itemInInventory.lock == 0 && itemInInventory.dayUse == 0) {
                vector.addElement(itemInInventory);
            }
        }
        return vector;
    }

    public static Vector getAllItemCharDrop() {
        Vector vector = new Vector();
        for (int i = 0; i < Char.inventory.size(); i++) {
            ItemInInventory itemInInventory = (ItemInInventory) Char.inventory.elementAt(i);
            if (isItemChar(Res.getItem(itemInInventory.charClazz, itemInInventory.idTemplate).type) && itemInInventory.dayUse == 0 && (itemInInventory.isItemDrop == 1 || itemInInventory.colorName != 0)) {
                vector.addElement(itemInInventory);
            }
        }
        return vector;
    }

    public static Vector getAllItem() {
        Vector vector = new Vector();
        for (int i = 0; i < Char.inventory.size(); i++) {
            ItemInInventory itemInInventory = (ItemInInventory) Char.inventory.elementAt(i);
            if (!isModelClothe(Res.getItem(itemInInventory.charClazz, itemInInventory.idTemplate).attb[9])) {
                vector.addElement(itemInInventory);
            }
        }
        return vector;
    }

    public void paint(Graphics graphics, int i, int i2) {
        GameData.paintIcon(graphics, Res.getItem(this.charClazz, this.idTemplate).idIcon, i, i2);
        short s = this.plusTemplate;
        if (s > 12) {
            Res.paintRectColor(graphics, i - 9, i2 - 9, 17, 17, this.count, color[s - 13], -260847);
            int i3 = this.count + 3;
            this.count = i3;
            if (i3 > 68) {
                this.count = 0;
            }
        }
    }

    public static Vector getAllItem(int i, int i2) {
        Vector vector = new Vector();
        for (int i3 = 0; i3 < Char.inventory.size(); i3++) {
            ItemInInventory itemInInventory = (ItemInInventory) Char.inventory.elementAt(i3);
            byte b = itemInInventory.colorName;
            if (b == i || b == i2) {
                vector.addElement(itemInInventory);
            }
        }
        return vector;
    }

    public String getTimeSell() {
        long jCurrentTimeMillis = this.timeEnd - System.currentTimeMillis();
        if (jCurrentTimeMillis <= 0) {
            return "Hết giờ";
        }
        if (jCurrentTimeMillis <= 0 || jCurrentTimeMillis / 60000 != 0) {
            return jCurrentTimeMillis > 0 ? "Còn lại: " + (jCurrentTimeMillis / 60000) + " phút" : "";
        }
        return jCurrentTimeMillis > 0 ? "Còn lại: " + (jCurrentTimeMillis / 1000) + " giây" : "";
    }
}
