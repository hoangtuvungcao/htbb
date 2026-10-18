package game.model;

import game.render.Canvas;
import game.render.Res;
import game.render.screen.CreateCharScr;
import game.render.screen.Font;
import game.render.screen.GameData;
import game.render.screen.WindowInfoScr;
import java.util.Vector;
import javax.microedition.lcdui.Graphics;

public class ItemInInventory {
    public String nameCharSell;
    public String nameCharBet;
    public short itemID;
    public short number;
    public short itemOldID;
    public byte potionType;
    public byte charClazz;
    public short idTemplate;
    public short plusTemplate;
    public short durable;
    public short mDurable;
    public int dayUse;
    public long lastTime;
    public byte maxNgoc;
    public short[] property;
    public byte numKham;
    public byte totalKham;
    public byte colorName;
    public byte viTriVe;
    public static boolean[][] attribShowedForType;
    static String[] na;
    static String[] nameNguHanh;
    static String[] nameHangItem;
    static String[] he;
    static String[] magic;
    public static byte typeItemBuy;
    public int prizeFix;
    public int prizeBet;
    public long timeEnd;
    private static int[] color;
    private static String[][] groupKick = {new String[]{"Quần", "Nhẫn dưới"}, new String[]{"Ngọc", "Găng"}, new String[]{"áo", "Dây chuyền"}, new String[]{"áo", "Dây chuyền"}, new String[]{"áo", "Dây chuyền"}, new String[]{"áo", "Dây chuyền"}, new String[]{"áo", "Dây chuyền"}, new String[]{"áo", "Dây chuyền"}, new String[]{"Nón", "Đồ cầm tay", "Phiến đá hệ", "Găng"}, new String[]{"Nhẫn dưới", "Quần"}, new String[]{"Nón", "Đồ cầm tay"}, new String[]{"Giày", "Nhẫn trên"}, new String[]{"Giày", "Nhẫn trên"}};
    private static String[][] kickHe = {new String[]{"Thép"}, new String[]{"Nước"}, new String[]{"Cỏ"}, new String[]{"Lửa"}, new String[]{"Đất"}};
    private static String[][] groupKickAnimal = {new String[]{"Nón"}, new String[]{"Bàn đạp"}, new String[]{"Vòng chân"}, new String[]{"Yên cương"}, new String[]{"Giáp"}};
    public static final String[] attrName = {"Tấn công", "Phòng thủ", "Né tránh", "Chính xác", "Chí mạng", "HP", "Phòng thủ đặc biệt", "Mã hình 1", "Mã hình 2", "Đánh dấu thời trang"};
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
    public short[] otherAtt = null;
    int count = Res.rnd(68);
    public String daySell = "";
    public byte kindItemInventory = 0;

    static {
        boolean[] zArr = new boolean[10];
        zArr[1] = true;
        zArr[2] = true;
        zArr[3] = true;
        zArr[6] = true;
        boolean[] zArr2 = new boolean[10];
        zArr2[1] = true;
        zArr2[2] = true;
        zArr2[3] = true;
        zArr2[6] = true;
        boolean[] zArr3 = new boolean[10];
        zArr3[1] = true;
        zArr3[2] = true;
        zArr3[3] = true;
        zArr3[6] = true;
        boolean[] zArr4 = new boolean[10];
        zArr4[0] = true;
        zArr4[3] = true;
        zArr4[4] = true;
        boolean[] zArr5 = new boolean[10];
        zArr5[0] = true;
        zArr5[3] = true;
        zArr5[4] = true;
        boolean[] zArr6 = new boolean[10];
        zArr6[0] = true;
        zArr6[3] = true;
        zArr6[4] = true;
        boolean[] zArr7 = new boolean[10];
        zArr7[0] = true;
        zArr7[3] = true;
        zArr7[4] = true;
        boolean[] zArr8 = new boolean[10];
        zArr8[0] = true;
        zArr8[3] = true;
        zArr8[4] = true;
        boolean[] zArr9 = new boolean[10];
        zArr9[0] = true;
        zArr9[3] = true;
        boolean[] zArr10 = new boolean[10];
        zArr10[0] = true;
        zArr10[4] = true;
        boolean[] zArr11 = new boolean[10];
        zArr11[1] = true;
        zArr11[2] = true;
        zArr11[6] = true;
        boolean[] zArr12 = new boolean[10];
        zArr12[1] = true;
        zArr12[3] = true;
        zArr12[4] = true;
        zArr12[6] = true;
        boolean[] zArr13 = new boolean[10];
        zArr13[3] = true;
        zArr13[4] = true;
        zArr13[5] = true;
        boolean[] zArr14 = new boolean[10];
        zArr14[0] = true;
        boolean[] zArr15 = new boolean[10];
        zArr15[1] = true;
        zArr15[6] = true;
        boolean[] zArr16 = new boolean[10];
        zArr16[0] = true;
        zArr16[3] = true;
        boolean[] zArr17 = new boolean[10];
        zArr17[1] = true;
        zArr17[6] = true;
        boolean[] zArr18 = new boolean[10];
        zArr18[0] = true;
        zArr18[4] = true;
        boolean[] zArr19 = new boolean[10];
        zArr19[1] = true;
        zArr19[6] = true;
        boolean[] zArr20 = new boolean[10];
        zArr20[1] = true;
        zArr20[6] = true;
        attribShowedForType = new boolean[][]{zArr, zArr2, zArr3, zArr4, zArr5, zArr6, zArr7, zArr8, zArr9, zArr10, zArr11, zArr12, zArr13, zArr14, zArr15, zArr16, zArr17, zArr18, zArr19, zArr20};
        na = new String[]{"Giáp nhẹ", "Giáp nặng", "Giáp vải", "Giáp nặng", "Giáp nhẹ", ""};
        nameNguHanh = new String[]{"Giảm st vật: ", "Giảm st ma: ", "Tăng tc: ", "Bỏ qua pt: ", "Phản st: ", "Bỏ qua tc ma", "Bỏ qua tc vật"};
        nameHangItem = new String[]{"", "hạng 1", "hạng 2", "hạng 3", "hạng 4", "hạng 5"};
        he = new String[]{"hệ Nước", "hệ Cỏ", "hệ Lửa", "hệ Đất", "hệ Thép"};
        magic = new String[]{"đặc biệt", "vật lý", ""};
        typeItemBuy = (byte) 2;
        color = new int[]{7667443, 3334656, 16705024};
    }

    public short[] getPoint() {
        return null;
    }

    public static boolean isModelClothe(int id) {
        return id == 1;
    }

    public static boolean isArmor(int type) {
        return type < 3;
    }

    public boolean isMyItem() {
        return this.nameCharSell.equals(Canvas.gameScr.mainChar.name);
    }

    public static boolean isAnimalArmor(int type) {
        return type >= 14 && type <= 18;
    }

    public static boolean isThanThu(int type) {
        return type == 25;
    }

    public static boolean isItemChar(int type) {
        return type < 13;
    }

    public ItemTemplate getTemplate() {
        return Res.getItem(this.charClazz, this.idTemplate);
    }

    public String[] getName() {
        String[] s = {"", "", ""};
        if (this.catagory == 3) {
            ItemTemplate item = Res.getItem(this.charClazz, this.idTemplate);
            String plus = "";
            if (this.plusTemplate > 0) {
                plus = new StringBuffer(String.valueOf(plus)).append(" +").append((int) this.plusTemplate).toString();
            }
            new StringBuffer(String.valueOf(plus)).append(" ").append(magic[this.magic_physic]).toString();
            s[0] = new StringBuffer(String.valueOf((int) this.colorName)).append(item.name).append(this.plusTemplate > 0 ? new StringBuffer(" +").append((int) this.plusTemplate).toString() : "").toString();
            s[1] = new StringBuffer("Giá bán: ").append(this.prize).append(" xu").toString();
            s[2] = new StringBuffer("Giá bid: ").append(this.prizeBet).append(" xu").toString();
        }
        return s;
    }

    public String compareItem(ItemInInventory it) {
        String s = "";
        if (this.catagory == 3) {
            String plus = "";
            ItemTemplate item = Res.getItem(this.charClazz, this.idTemplate);
            if (!isModelClothe(item.attb[9])) {
                if (this.plusTemplate > 0) {
                    plus = new StringBuffer(String.valueOf(plus)).append(" +").append((int) this.plusTemplate).toString();
                }
                String plus2 = new StringBuffer(String.valueOf(plus)).append(" ").append(magic[this.magic_physic]).toString();
                if (this.hangItem > -1) {
                    plus2 = new StringBuffer(String.valueOf(plus2)).append(" ").append(nameHangItem[this.hangItem]).toString();
                }
                if (this.heItem > -1) {
                    plus2 = new StringBuffer(String.valueOf(plus2)).append(" ").append(he[this.heItem]).toString();
                }
                String s2 = new StringBuffer(String.valueOf(s)).append(setTemp(new StringBuffer(String.valueOf(item.name)).append(plus2).toString(), new StringBuffer().append((int) this.colorName).toString())).toString();
                // pokemon: dòng loại đồ theo tên Pokémon mới
                if (game.pokemon.BangTrangBi.tenLoai(item.type) != null) {
                    s2 = new StringBuffer(String.valueOf(s2)).append("\n0Loại: ").append(game.pokemon.BangTrangBi.tenLoai(item.type)).toString();
                }
                if (!this.nameCharSeal.equals("")) {
                    s2 = new StringBuffer(String.valueOf(s2)).append("\n0").append(this.nameCharSeal.toUpperCase()).append(" tạo").toString();
                }
                if (this.lock == 1) {
                    s2 = new StringBuffer(String.valueOf(s2)).append("\nĐã khoá").toString();
                }
                if (isArmor(item.type) && item.type != 13 && !isAnimalArmor(item.type)) {
                    s2 = new StringBuffer(String.valueOf(s2)).append("\n").append(na[this.clazz]).toString();
                }
                if (this.clazz != -1 && item.type != 13 && item.type != 19 && !isAnimalArmor(item.type)) {
                    s2 = new StringBuffer(String.valueOf(s2)).append("\n").append(Res.nameClazz[this.clazz]).toString();
                }
                if (item.level != -1) {
                    s2 = new StringBuffer(String.valueOf(s2)).append("\nYêu cầu lv: ").append((int) this.level).toString();
                }
                s = new StringBuffer(String.valueOf(s2)).append("\nĐộ bền: ").append((int) this.durable).toString();
                byte b = 0;
                while (true) {
                    byte i = b;
                    if (i >= this.allAttribute.size()) {
                        try {
                            break;
                        } catch (Exception e) {
                        }
                    } else {
                        InfoAttributeItem name = (InfoAttributeItem) this.allAttribute.elementAt(i);
                        String temp = new StringBuffer(String.valueOf(name.getName(this.clazz))).append(name.getValue()).append(name.isPercent() ? "%" : "").toString();
                        s = new StringBuffer(String.valueOf(s)).append(setTemp(temp, new StringBuffer(String.valueOf((int) name.getColorPaint(this.beKicked == 1))).toString())).toString();
                        for (int j = 0; j < it.allAttribute.size(); j++) {
                            InfoAttributeItem name1 = (InfoAttributeItem) this.allAttribute.elementAt(i);
                            if (name1.getID() == name.getID()) {
                                if (name1.getValueAtt() > name.getValueAtt()) {
                                    s = new StringBuffer(String.valueOf(s)).append("~1").append(name1.getValueAtt() - name.getValueAtt()).toString();
                                } else if (name1.getValueAtt() > name.getValueAtt()) {
                                    s = new StringBuffer(String.valueOf(s)).append("~0").append(name.getValueAtt() - name1.getValueAtt()).toString();
                                }
                            }
                        }
                        b = (byte) (i + 1);
                    }
                }
                if (this.hangItem > 0 && this.hangItem < 5 && item.type != 13 && item.type < 13) {
                    if (item.type != 8) {
                        s = new StringBuffer(String.valueOf(s)).append(setTemp(new StringBuffer("cần ").append(groupKick[item.type][0]).append(" hoặc ").append(groupKick[item.type][1]).append(" hệ ").append(kickHe[this.heItem][0]).append(" để kích hệ").toString(), "0")).toString();
                    } else if (this.viTriVe == 1) {
                        s = new StringBuffer(String.valueOf(s)).append(setTemp(new StringBuffer("Nhẫn trên cần ").append(groupKick[item.type][0]).append(" hoặc ").append(groupKick[item.type][1]).append(" hệ ").append(kickHe[this.heItem][0]).append(" để kích hệ").toString(), "0")).toString();
                    } else {
                        s = new StringBuffer(String.valueOf(s)).append(setTemp(new StringBuffer("Nhẫn dưới cần ").append(groupKick[item.type][2]).append(" hoặc ").append(groupKick[item.type][3]).append(" hệ ").append(kickHe[this.heItem][0]).append(" để kích hệ").toString(), "0")).toString();
                    }
                }
                if (this.hangItem > 0 && this.hangItem < 5 && isAnimalArmor(item.type)) {
                    s = new StringBuffer(String.valueOf(s)).append(setTemp(new StringBuffer("cần ").append(groupKickAnimal[item.type - 14][0]).append(" hệ ").append(kickHe[this.heItem][0]).append(" để kích hệ").toString(), "0")).toString();
                }
            } else {
                s = new StringBuffer(String.valueOf(new StringBuffer(String.valueOf(new StringBuffer(String.valueOf(new StringBuffer(String.valueOf(new StringBuffer(String.valueOf(s)).append(item.name).toString())).append("\nTấn công tăng: ").append((int) item.attb[0]).append("%").toString())).append("\nThủ tăng: ").append((int) item.attb[1]).append("%").toString())).append("\nHP tăng: ").append((int) item.attb[2]).toString())).append("\nMP tăng: ").append((int) item.attb[3]).toString();
            }
            if (this.dayUse != 0) {
                long time = ((long) this.dayUse) - ((System.currentTimeMillis() - this.lastTime) / 60000);
                if (time < 0) {
                    time = 1;
                }
                s = new StringBuffer(String.valueOf(s)).append(setTemp(new StringBuffer("\nThời gian còn lại: ").append(time / 60 > 0 ? new StringBuffer(String.valueOf(time / 60)).append("h ").toString() : "").append(time % 60 > 0 ? new StringBuffer(String.valueOf(time % 60)).append("p").toString() : "").toString(), "0")).toString();
            }
            if (this.timeEnd > 0) {
                s = new StringBuffer(String.valueOf(new StringBuffer(String.valueOf(new StringBuffer(String.valueOf(new StringBuffer(String.valueOf(new StringBuffer(String.valueOf(s)).append(setTemp(this.nameCharSell, "0")).toString())).append(setTemp(this.daySell, "0")).toString())).append(setTemp(new StringBuffer("Bid: ").append(this.prizeBet).append(" xu").toString(), "0")).toString())).append(setTemp(this.nameCharBet.equals("") ? "----" : new StringBuffer(String.valueOf(this.nameCharBet)).append(" bid").toString(), "0")).toString())).append(setTemp(getTimeSell(), "0")).toString();
            }
        }
        return s;
    }

    public String getDescription(boolean showResellPrice) {
        String s = "";
        if (this.catagory == 3) {
            String plus = "";
            ItemTemplate item = Res.getItem(this.charClazz, this.idTemplate);
            if (!isModelClothe(item.attb[9])) {
                if (this.plusTemplate > 0) {
                    plus = new StringBuffer(String.valueOf(plus)).append(" +").append((int) this.plusTemplate).toString();
                }
                String plus2 = new StringBuffer(String.valueOf(plus)).append(" ").append(magic[this.magic_physic]).toString();
                if (this.hangItem > -1) {
                    plus2 = new StringBuffer(String.valueOf(plus2)).append(" ").append(nameHangItem[this.hangItem]).toString();
                }
                if (this.heItem > -1) {
                    plus2 = new StringBuffer(String.valueOf(plus2)).append(" ").append(he[this.heItem]).toString();
                }
                String s2 = new StringBuffer(String.valueOf(s)).append(setTemp(new StringBuffer(String.valueOf(item.name)).append(plus2).toString(), new StringBuffer().append((int) this.colorName).toString())).toString();
                // pokemon: dòng loại đồ theo tên Pokémon mới
                if (game.pokemon.BangTrangBi.tenLoai(item.type) != null) {
                    s2 = new StringBuffer(String.valueOf(s2)).append("\n0Loại: ").append(game.pokemon.BangTrangBi.tenLoai(item.type)).toString();
                }
                if (!this.nameCharSeal.equals("")) {
                    s2 = new StringBuffer(String.valueOf(s2)).append("\n0").append(this.nameCharSeal.toLowerCase()).append(this.nameCharSeal.indexOf("-") > -1 ? "" : " tạo").toString();
                }
                if (this.lock == 1) {
                    s2 = new StringBuffer(String.valueOf(s2)).append("\nĐã khoá").toString();
                }
                if (isArmor(item.type) && item.type != 13 && !isAnimalArmor(item.type)) {
                    s2 = new StringBuffer(String.valueOf(s2)).append("\n").append(na[this.clazz]).toString();
                }
                if (this.clazz != -1 && item.type != 13 && item.type != 19 && !isAnimalArmor(item.type)) {
                    s2 = new StringBuffer(String.valueOf(s2)).append("\n").append(Res.nameClazz[this.clazz]).toString();
                }
                if (this.isSelling) {
                    s2 = new StringBuffer(String.valueOf(s2)).append("\nGiá bán: ").append(Canvas.getMoneys(this.prize)).append("~").toString();
                } else if (showResellPrice) {
                    s2 = new StringBuffer(String.valueOf(s2)).append("\nGiá bán lại: ").append(Canvas.getMoneys((item.price / 5) + (this.plusTemplate * 1000))).append("~").toString();
                }
                if (item.level != -1) {
                    s2 = new StringBuffer(String.valueOf(s2)).append("\nYêu cầu lv: ").append((int) this.level).toString();
                }
                s = new StringBuffer(String.valueOf(s2)).append("\nĐộ bền: ").append((int) this.durable).toString();
                byte b = 0;
                while (true) {
                    byte i = b;
                    if (i >= this.allAttribute.size()) {
                        try {
                            break;
                        } catch (Exception e) {
                        }
                    } else {
                        InfoAttributeItem name = (InfoAttributeItem) this.allAttribute.elementAt(i);
                        String temp = new StringBuffer(String.valueOf(name.getName(this.clazz))).append(name.getValue()).append(name.isPercent() ? "%" : "").toString();
                        s = new StringBuffer(String.valueOf(s)).append(setTemp(temp, new StringBuffer(String.valueOf((int) name.getColorPaint(this.beKicked == 1))).toString())).toString();
                        b = (byte) (i + 1);
                    }
                }
                if (this.hangItem > 0 && this.hangItem < 5 && item.type != 13 && item.type < 13) {
                    if (item.type != 8) {
                        s = new StringBuffer(String.valueOf(s)).append(setTemp(new StringBuffer("cần ").append(groupKick[item.type][0]).append(" hoặc ").append(groupKick[item.type][1]).append(" hệ ").append(kickHe[this.heItem][0]).append(" để kích hệ").toString(), "0")).toString();
                    } else if (this.viTriVe == 1) {
                        s = new StringBuffer(String.valueOf(s)).append(setTemp(new StringBuffer("Nhẫn trên cần ").append(groupKick[item.type][0]).append(" hoặc ").append(groupKick[item.type][1]).append(" hệ ").append(kickHe[this.heItem][0]).append(" để kích hệ").toString(), "0")).toString();
                    } else {
                        s = new StringBuffer(String.valueOf(s)).append(setTemp(new StringBuffer("Nhẫn dưới cần ").append(groupKick[item.type][2]).append(" hoặc ").append(groupKick[item.type][3]).append(" hệ ").append(kickHe[this.heItem][0]).append(" để kích hệ").toString(), "0")).toString();
                    }
                }
                if (this.hangItem > 0 && this.hangItem < 5 && isAnimalArmor(item.type)) {
                    s = new StringBuffer(String.valueOf(s)).append(setTemp(new StringBuffer("cần ").append(groupKickAnimal[item.type - 14][0]).append(" hệ ").append(kickHe[this.heItem][0]).append(" để kích hệ").toString(), "0")).toString();
                }
            } else {
                s = new StringBuffer(String.valueOf(s)).append(item.name).toString();
                if (!this.nameCharSeal.equals("")) {
                    s = new StringBuffer(String.valueOf(s)).append("\n0").append(this.nameCharSeal.toLowerCase()).append(this.nameCharSeal.indexOf("-") > -1 ? "" : " tạo").toString();
                }
                byte b2 = 0;
                while (true) {
                    byte i2 = b2;
                    if (i2 >= this.allAttribute.size()) {
                        break;
                    }
                    InfoAttributeItem name2 = (InfoAttributeItem) this.allAttribute.elementAt(i2);
                    String temp2 = new StringBuffer(String.valueOf(name2.getName(this.clazz))).append(name2.getValue()).append(name2.isPercent() ? "%" : "").toString();
                    s = new StringBuffer(String.valueOf(s)).append(setTemp(temp2, new StringBuffer(String.valueOf((int) name2.getColorPaint(this.beKicked == 1))).toString())).toString();
                    b2 = (byte) (i2 + 1);
                }
            }
            if (this.dayUse != 0) {
                long time = ((long) this.dayUse) - ((System.currentTimeMillis() - this.lastTime) / 60000);
                if (time < 0) {
                    time = 1;
                }
                s = new StringBuffer(String.valueOf(s)).append(setTemp(new StringBuffer("\nThời gian còn lại: ").append(time / 60 > 0 ? new StringBuffer(String.valueOf(time / 60)).append("h ").toString() : "").append(time % 60 > 0 ? new StringBuffer(String.valueOf(time % 60)).append("p").toString() : "").toString(), "0")).toString();
            }
            if (this.timeEnd > 0) {
                s = new StringBuffer(String.valueOf(new StringBuffer(String.valueOf(new StringBuffer(String.valueOf(new StringBuffer(String.valueOf(new StringBuffer(String.valueOf(s)).append(setTemp(this.nameCharSell, "0")).toString())).append(setTemp(this.daySell, "0")).toString())).append(setTemp(new StringBuffer("Bid: ").append(this.prizeBet).append(" xu").toString(), "0")).toString())).append(setTemp(this.nameCharBet.equals("") ? "----" : new StringBuffer(String.valueOf(this.nameCharBet)).append(" bid").toString(), "0")).toString())).append(setTemp(getTimeSell(), "0")).toString();
            }
        }
        return s;
    }

    public static String setTemp(String temp, String typeFont) {
        String str = "";
        String[] tem = Font.borderFont.splitFontBStrInLine(temp, 95);
        for (String str2 : tem) {
            str = new StringBuffer(String.valueOf(str)).append("\n").append(typeFont).append(str2).toString();
        }
        return str;
    }

    public String getTemplateDescription() {
        String s;
        String s2 = "";
        try {
            String[] na2 = {"Giáp nhẹ", "Giáp nặng", "Giáp vải", "Giáp nặng", "Giáp nhẹ"};
            if (this.catagory == 3) {
                String plus = "";
                ItemTemplate item = Res.getItem(this.charClazz, this.idTemplate);
                if (this.idTemplate < 264 || this.idTemplate > 267) {
                    if (this.plusTemplate > 0) {
                        plus = new StringBuffer(String.valueOf(plus)).append("+").append((int) this.plusTemplate).toString();
                    }
                    String s3 = new StringBuffer(String.valueOf(new StringBuffer(String.valueOf(s2)).append("\n").append(item.name).toString())).append(" ").append(plus).toString();
                    if (game.pokemon.BangTrangBi.tenLoai(item.type) != null) {
                        s3 = new StringBuffer(String.valueOf(s3)).append("\n0Loại: ").append(game.pokemon.BangTrangBi.tenLoai(item.type)).toString();
                    }
                    if ((item.id < 79 || item.id > 137) && item.type != 13 && isAnimalArmor(item.type)) {
                        s3 = new StringBuffer(String.valueOf(s3)).append("\n").append(na2[this.clazz]).toString();
                    }
                    if (item.id > 113 && item.id <= 137 && item.type != 13) {
                        s3 = new StringBuffer(String.valueOf(s3)).append("\n").append(Res.nameClazz[this.clazz]).toString();
                    }
                    if (item.ndayLoan == 0) {
                        s = new StringBuffer(String.valueOf(s3)).append("\nGiá: ").append(Canvas.getMoneys(item.price + (this.plusTemplate * 1000))).append(" ").append("xu").toString();
                    } else {
                        s = new StringBuffer(String.valueOf(s3)).append("\nGiá: ").append(Canvas.getMoneys(item.price + this.plusTemplate)).append(" Lượng").toString();
                    }
                    if (this.level != -1) {
                        s = new StringBuffer(String.valueOf(s)).append("\nYêu cầu lv: ").append((int) this.level).toString();
                    }
                    s2 = new StringBuffer(String.valueOf(s)).append("\nĐộ bền: ").append((int) this.durable).toString();
                    if (item.clazz != -1 && item.type != 13) {
                        s2 = new StringBuffer(String.valueOf(s2)).append("\nDành cho: ").append(CreateCharScr.clazz[this.clazz][CreateCharScr.selectGender].name).toString();
                    }
                    int[] attb = Res.getAttributeItem(this.charClazz, this.idTemplate, item.type);
                    if (item.type < 2 || item.type == 10 || item.type == 11 || isAnimalArmor(item.type)) {
                        if (typeItemBuy == 0) {
                            attb[6] = attb[1];
                            attb[1] = attb[1] / 10;
                        } else {
                            attb[6] = attb[1] / 10;
                        }
                    }
                    for (int i = 0; i < 10; i++) {
                        if (attribShowedForType[item.type][i]) {
                            s2 = new StringBuffer(String.valueOf(s2)).append("\n").append(attrName[i]).append(": ").append(attb[i]).toString();
                        }
                    }
                } else {
                    s2 = new StringBuffer(String.valueOf(new StringBuffer(String.valueOf(new StringBuffer(String.valueOf(new StringBuffer(String.valueOf(new StringBuffer(String.valueOf(s2)).append(item.name).toString())).append("\nTấn công tăng: ").append((int) item.attb[0]).append("%").toString())).append("\nThủ tăng: ").append((int) item.attb[1]).append("%").toString())).append("\nHP tăng: ").append((int) item.attb[2]).toString())).append("\nMP tăng: ").append((int) item.attb[3]).toString();
                }
                if (item.ndayLoan > 0) {
                    s2 = new StringBuffer(String.valueOf(s2)).append(setTemp(new StringBuffer("\nThời hạn: ").append((int) item.ndayLoan).append(" Phút.").toString(), "0")).toString();
                }
            }
            if (this.catagory == 4 && this.isShopItem) {
                String s4 = new StringBuffer(String.valueOf(new StringBuffer().append(MainChar.listPotion[this.potionType].name).toString())).append("\nGiá: ").append(Canvas.getMoneys(Res.potionTemplates[this.potionType].price)).append("$").toString();
                if (Res.potionTemplates[this.potionType].recovered >= 80) {
                    s4 = new StringBuffer(String.valueOf(s4)).append("\nTăng ").append((int) Res.potionTemplates[this.potionType].recovered).append(" ").append(MainChar.listPotion[this.potionType].name2).toString();
                } else if (Res.potionTemplates[this.potionType].recovered == 0) {
                    s4 = new StringBuffer(String.valueOf(s4)).append("\nKhăn đeo dùng khi đánh nhau.").toString();
                } else if (Res.potionTemplates[this.potionType].recovered == -1) {
                    s4 = new StringBuffer(String.valueOf(s4)).append("\nDịch chuyển tức thời về làng.").toString();
                }
                int n1 = Canvas.gameScr.mainChar.potions[this.potionType];
                int n2 = 0;
                for (int i2 = 0; i2 < WindowInfoScr.gI().readyBuy.size(); i2++) {
                    ItemInInventory item2 = (ItemInInventory) WindowInfoScr.gI().readyBuy.elementAt(i2);
                    if (item2.potionType == this.potionType) {
                        n2 = item2.number;
                    }
                }
                if (n2 == 0) {
                    s2 = new StringBuffer(String.valueOf(s4)).append("\nĐang có: ").append(n1).append(" cái.").toString();
                } else {
                    s2 = new StringBuffer(String.valueOf(s4)).append("\nĐang có: ").append(n1).append("+").append(n2).append(" cái.").toString();
                }
                if (n1 + n2 >= 999) {
                    s2 = new StringBuffer(String.valueOf(s2)).append("\n(tối đa)").toString();
                }
            }
        } catch (Exception e) {
            Cout.println("LOI ROI ");
        }
        return s2;
    }

    public String getInfoItemUserSell() {
        String plus = "";
        ItemTemplate item = Res.getItem(this.charClazz, this.idTemplate);
        if (this.plusTemplate > 0) {
            plus = new StringBuffer(String.valueOf(plus)).append(" +").append((int) this.plusTemplate).toString();
        }
        String plus2 = new StringBuffer(String.valueOf(plus)).append(" ").append(magic[this.magic_physic]).toString();
        if (this.hangItem > -1) {
            plus2 = new StringBuffer(String.valueOf(plus2)).append(" ").append(nameHangItem[this.hangItem]).toString();
        }
        if (this.heItem > -1) {
            plus2 = new StringBuffer(String.valueOf(plus2)).append(" ").append(he[this.heItem]).toString();
        }
        String s = new StringBuffer(String.valueOf("")).append(setTemp(new StringBuffer(String.valueOf(item.name)).append(plus2).toString(), new StringBuffer().append((int) this.colorName).toString())).toString();
        if (!this.nameCharSeal.equals("")) {
            s = new StringBuffer(String.valueOf(s)).append("\n0").append(this.nameCharSeal.toUpperCase()).append(" tạo").toString();
        }
        if (this.level != -1) {
            s = new StringBuffer(String.valueOf(s)).append("\nYêu cầu lv: ").append((int) this.level).toString();
        }
        String s2 = new StringBuffer(String.valueOf(s)).append("\nĐộ bền: ").append((int) this.durable).toString();
        if (this.clazz != -1) {
            s2 = new StringBuffer(String.valueOf(s2)).append("\nDòng: ").append(CreateCharScr.clazz[this.clazz][CreateCharScr.selectGender].name).toString();
        }
        byte b = 0;
        while (true) {
            byte i = b;
            if (i >= this.allAttribute.size()) {
                break;
            }
            InfoAttributeItem name = (InfoAttributeItem) this.allAttribute.elementAt(i);
            String temp = new StringBuffer(String.valueOf(name.getName(this.clazz))).append(name.getValue()).append(name.isPercent() ? "%" : "").toString();
            s2 = new StringBuffer(String.valueOf(s2)).append(setTemp(temp, new StringBuffer(String.valueOf((int) name.getColorPaint(false))).toString())).toString();
            b = (byte) (i + 1);
        }
        if (this.prize > 0) {
            s2 = new StringBuffer(String.valueOf(s2)).append("\nGiá bán:\n0").append(Canvas.getMoneys(this.prize)).toString();
            Cout.println(new StringBuffer(String.valueOf(this.prize)).append(" GIA BAN ").append(Canvas.getMoneys(this.prize)).toString());
        }
        return s2;
    }

    public static Vector getAllItem(int colorItem) {
        Vector all = new Vector();
        for (int i = 0; i < Char.inventory.size(); i++) {
            ItemInInventory item = (ItemInInventory) Char.inventory.elementAt(i);
            if (item.colorName == colorItem) {
                all.addElement(item);
            }
        }
        return all;
    }

    public static Vector getAllItem(int colorItem, int coloritem2) {
        Vector all = new Vector();
        for (int i = 0; i < Char.inventory.size(); i++) {
            ItemInInventory item = (ItemInInventory) Char.inventory.elementAt(i);
            if (item.colorName == colorItem || item.colorName == coloritem2) {
                all.addElement(item);
            }
        }
        return all;
    }

    public static Vector getAllItemCharExcepColor(int colorItem) {
        Vector all = new Vector();
        for (int i = 0; i < Char.inventory.size(); i++) {
            ItemInInventory item = (ItemInInventory) Char.inventory.elementAt(i);
            if (item.colorName != colorItem && isItemChar(item.getTemplate().type)) {
                all.addElement(item);
            }
        }
        return all;
    }

    public static Vector getAllItemAnimal(int colorItem, int hang) {
        Vector all = new Vector();
        for (int i = 0; i < Char.inventory.size(); i++) {
            ItemInInventory item = (ItemInInventory) Char.inventory.elementAt(i);
            if (item.colorName == colorItem && item.hangItem == hang && isAnimalArmor(item.getTemplate().type)) {
                all.addElement(item);
            }
        }
        return all;
    }

    public static Vector getAllItemCanSell() {
        Vector all = new Vector();
        for (int i = 0; i < Char.inventory.size(); i++) {
            ItemInInventory it = (ItemInInventory) Char.inventory.elementAt(i);
            ItemTemplate item = Res.getItem(it.charClazz, it.idTemplate);
            if (!isModelClothe(item.attb[9]) && it.lock == 0 && it.dayUse == 0) {
                all.addElement(it);
            }
        }
        return all;
    }

    public static Vector getAllItemChar() {
        Vector all = new Vector();
        for (int i = 0; i < Char.inventory.size(); i++) {
            ItemInInventory it = (ItemInInventory) Char.inventory.elementAt(i);
            ItemTemplate item = Res.getItem(it.charClazz, it.idTemplate);
            if (isItemChar(item.type) && it.dayUse == 0) {
                all.addElement(it);
            }
        }
        return all;
    }

    public static Vector getAllItemCharDrop() {
        Vector all = new Vector();
        for (int i = 0; i < Char.inventory.size(); i++) {
            ItemInInventory it = (ItemInInventory) Char.inventory.elementAt(i);
            ItemTemplate item = Res.getItem(it.charClazz, it.idTemplate);
            if (isItemChar(item.type) && it.dayUse == 0 && (it.isItemDrop == 1 || it.colorName != 0)) {
                all.addElement(it);
            }
        }
        return all;
    }

    public static Vector getAllItem() {
        Vector all = new Vector();
        for (int i = 0; i < Char.inventory.size(); i++) {
            ItemInInventory it = (ItemInInventory) Char.inventory.elementAt(i);
            ItemTemplate item = Res.getItem(it.charClazz, it.idTemplate);
            if (!isModelClothe(item.attb[9])) {
                all.addElement(it);
            }
        }
        return all;
    }

    public void paint(Graphics g, int x, int y) {
        ItemTemplate item = Res.getItem(this.charClazz, this.idTemplate);
        // pokemon: khung màu độ hiếm + "+N" cường hóa
        int hiem = game.pokemon.KieuO.mauHiem(this.colorName);
        if (hiem >= 0) {
            game.pokemon.KieuO.khungHiem(g, x - 9, y - 9, 18, hiem);
        }
        GameData.paintIcon(g, item.idIcon, x, y);
        if (this.plusTemplate > 0) {
            game.pokemon.KieuO.capCuong(g, x - 9, y - 9, this.plusTemplate);
        }
        if (this.plusTemplate > 12) {
            Res.paintRectColor(g, x - 9, y - 9, 17, 17, this.count, color[this.plusTemplate - 13], 16516369);
            this.count += 3;
            if (this.count > 68) {
                this.count = 0;
            }
        }
    }

    public String getTimeSell() {
        long time = this.timeEnd - System.currentTimeMillis();
        if (time <= 0) {
            return "Hết giờ";
        }
        if (time <= 0 || time / 60000 != 0) {
            return time > 0 ? new StringBuffer("Còn lại: ").append(time / 60000).append(" phút").toString() : "";
        }
        return time > 0 ? new StringBuffer("Còn lại: ").append(time / 1000).append(" giây").toString() : "";
    }
}
