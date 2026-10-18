package game.render.screen;

import android.graphics.Bitmap;
import game.core.j2me.Graphics;
import game.core.j2me.Image;
import game.model.Command;
import game.model.IAction;
import game.model.InfoAttributeItem;
import game.model.ItemInInventory;
import game.model.ItemTemplate;
import game.model.Layer;
import game.model.PageScr;
import game.model.Scroll;
import game.model.ScrollResult;
import game.networklogic.GameService;
import game.render.GCanvas;
import game.render.Res;
import java.util.Vector;

public class ShopOnline extends Screen {
    public static short level;
    public static short wStr;
    private byte[] arrayKhamNgoc;
    public Command cmdBuy;
    public Command cmdFind;
    public Command cmdLastCenter;
    public Command cmdLastLeft;
    public Command cmdMenu;
    public Command cmdSell;
    public Command cmdShowText;
    public Scroll cmrShowTxt;
    private int countL;
    public int h0;
    public int hShowTxt;
    private int idColor;
    private int idPham;
    private int idType;
    private int indexMainTile;
    private boolean isFocusMainTab;
    public boolean isShowText;
    public Screen lastScreen;
    public Scroll scl;
    public int select;
    public int selectTxt;
    public int w0;
    public int wShowTxt;
    public int x;
    private int xRunL;
    private int xRunR;
    public int xShowTxt;
    public int y;
    public int yShowTxt;
    public static Vector ALL_ITEM = new Vector();
    public static String[] tile = {"Chợ", "Tìm", "Đang bán", "Hành trang", "Kho", "Bid"};
    public static short[] nPage = {0, 0, 0, 0, 0, 0};
    public static short[] indexPage = {0, 0, 0, 0, 0, 0};
    public static String[][] tileFind = {new String[]{"Loại:", "Level:", "Phẩm:", "Màu:", "Cộng:"}, new String[]{"N.vật"}, new String[0]};
    public static String[] nameItem = {"áo", "Quần", "Nón", "Than lửa", "Hạt giống thần", "Bút", "Búa", "Cung", "Nhẫn", "Dây chuyền", "Giày", "Găng tay", "Phiến đá hệ", "Giáp thú cưỡi", "Vòng chân", "Nón thú cưỡi", "Bàn đạp", "Yên cương", "Ruy băng"};
    public static byte[] ID_TYPE_ITEM = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 14, 15, 16, 17, 18, 19};
    public static String[] Pham = {"Hạng 1", "Hạng 2", "Hạng 3", "Hạng 4", "Khác"};
    public static String[] Color = {"Trắng", "Xanh", "Đỏ", "Hoàn mỹ"};
    public static String pham = "";
    public static String mau = "";
    public static long money = 0;
    private int idLevel = 1;
    private int idPlus = 0;
    private boolean isSelectTypeFind = true;
    private byte typeSearch = 0;
    public String[] arrInfo = {""};
    public String nameChar = "";
    Bitmap img = null;
    public int wShowLv = 110;
    public String price = "10.000.000.000";
    public String strLv = "";
    public Command cmdDoNothing = new Command("", new IAction() {
        @Override
        public void perform() {
        }
    });

    public void doAcceptSell() {
    }

    protected void doSortBuyLevel() {
    }

    protected void doSortBuyPrice() {
    }

    public void paintLevelAndPrice(Graphics graphics, int i, int i2, boolean z, boolean z2, ItemInInventory itemInInventory) {
    }

    public void reset() {
        this.select = -1;
        this.selectTxt = 0;
        level = (short) 0;
        pham = "";
        mau = "";
        this.indexMainTile = 1;
        this.isSelectTypeFind = true;
        this.isFocusMainTab = !GCanvas.isSmartPhone;
        this.idType = 0;
        this.idPham = 0;
        this.idColor = 0;
        this.idLevel = 1;
        this.isShowText = false;
        this.arrInfo = new String[]{""};
        this.scl.clear();
        this.cmrShowTxt.clear();
        setCmdLeft();
    }

    public void setShowTxt(String[] strArr) {
        String[] strArr2 = new String[1];
        this.arrInfo = strArr;
        this.isShowText = true;
    }

    public ShopOnline() {
        ALL_ITEM.addElement(new Vector());
        ALL_ITEM.addElement(new Vector());
        ALL_ITEM.addElement(new Vector());
        ALL_ITEM.addElement(new Vector());
        ALL_ITEM.addElement(new Vector());
        ALL_ITEM.addElement(new Vector());
    }

    public void paintNameItemSelect(Graphics graphics, int i, int i2, String str, Font font, int i3) {
        int i4 = i2 + 7;
        graphics.drawImage(Res.imgBar, i + 100 + (this.select == i3 ? this.countL : 0), i4, 3);
        graphics.drawRegion(Res.imgBar, 0, 0, 11, 7, 2, i - (this.select == i3 ? this.countL : 0), i4, 3);
        font.drawString(graphics, str, i + 50, i2, 2);
    }

    @Override
    public void switchToMe() {
        init();
        super.switchToMe();
    }

    @Override
    public void switchToMe(Screen screen) {
        this.lastScreen = screen;
        init();
        super.switchToMe(screen);
    }

    @Override
    public void init() {
        if (GCanvas.isSmartPhone) {
            tileFind[1][0] = "Pokémon";
        }
        if (this.img == null) {
            try {
                this.img = Image.createImage("/0.png");
            } catch (Exception unused) {
            }
        }
        int i = GCanvas.w - 6;
        int i2 = GCanvas.h - 30;
        if (i > 320) {
            i = 320;
        }
        if (i2 > 240) {
            i2 = 240;
        }
        this.w0 = i;
        this.h0 = i2;
        this.x = (GCanvas.w / 2) - (this.w0 / 2);
        int i3 = ((GCanvas.h / 2) - (this.h0 / 2)) - 10;
        this.y = i3;
        if (i3 < 8) {
            this.y = 8;
        }
        int i4 = GCanvas.w - 40;
        this.wShowTxt = i4;
        if (i4 > 200) {
            this.wShowTxt = 200;
        }
        int i5 = GCanvas.h - 80;
        this.hShowTxt = i5;
        if (i5 > 180) {
            this.hShowTxt = 180;
        }
        this.xShowTxt = (GCanvas.w / 2) - (this.wShowTxt / 2);
        int i6 = (GCanvas.h / 2) - (this.hShowTxt / 2);
        this.yShowTxt = i6;
        if (i6 < 5) {
            this.yShowTxt = 5;
        }
        for (int i7 = 0; i7 < tileFind[0].length; i7++) {
            if (wStr < Font.normalFont[0].getWidth(tileFind[0][i7])) {
                wStr = (short) Font.normalFont[0].getWidth(tileFind[0][i7]);
            }
        }
        wStr = (short) (wStr + 5);
        this.select = -1;
        this.isFocusMainTab = !GCanvas.isSmartPhone;
        this.isShowText = false;
        this.idType = 0;
        this.idPham = 0;
        this.idColor = 0;
        this.idLevel = 1;
        this.center = new Command(GCanvas.isSmartPhone ? "" : "Chọn", new IAction() {
            @Override
            public void perform() {
                ShopOnline.this.doselect();
            }
        });
        this.cmdFind = new Command("Tìm", new IAction() {
            @Override
            public void perform() {
                ShopOnline.this.doFind();
                ShopOnline shopOnline = ShopOnline.this;
                shopOnline.left = shopOnline.cmdMenu;
            }
        });
        this.right = new Command("Đóng", new IAction() {
            @Override
            public void perform() {
                if (ShopOnline.this.isShowText) {
                    ShopOnline.this.isShowText = false;
                    ShopOnline.this.setCmdLeft();
                } else {
                    ShopOnline.this.lastScreen.switchToMe();
                    ShopOnline.this.reset();
                }
            }
        });
        this.cmdBuy = new Command("", new IAction() {
            @Override
            public void perform() {
            }
        });
        this.cmdSell = new Command("Menu", new IAction() {
            @Override
            public void perform() {
                Vector vector = new Vector();
                ShopOnline.this.doCmdSell(vector);
                GCanvas.menu.startAt(vector, 0);
            }
        });
        this.cmdMenu = new Command("Menu", new IAction() {
            @Override
            public void perform() {
                ShopOnline.this.isShowText = false;
                Vector vector = new Vector();
                new Command("Mua", new IAction() {
                    @Override
                    public void perform() {
                        ShopOnline.this.doAcceptBuy(1, (ItemInInventory) ((Vector) ShopOnline.ALL_ITEM.elementAt(ShopOnline.this.indexMainTile)).elementAt(ShopOnline.this.select));
                    }
                });
                new Command("Tìm", new IAction() {
                    @Override
                    public void perform() {
                        ShopOnline.this.reset();
                        ShopOnline.this.typeSearch = (byte) 0;
                        ShopOnline.this.left = ShopOnline.this.cmdFind;
                    }
                });
                Command command = new Command("Tìm theo người bán", new IAction() {
                    @Override
                    public void perform() {
                        ShopOnline.this.reset();
                        ShopOnline.this.typeSearch = (byte) 1;
                        ShopOnline.this.left = ShopOnline.this.cmdFind;
                    }
                });
                if (((Vector) ShopOnline.ALL_ITEM.elementAt(ShopOnline.this.indexMainTile)).size() > 0) {
                    command = new Command("Kết quả", new IAction() {
                        @Override
                        public void perform() {
                            ShopOnline.this.isSelectTypeFind = false;
                            ShopOnline.this.select = -1;
                        }
                    });
                }
                vector.addElement(command);
                GCanvas.menu.startAt(vector, 0);
            }
        });
        this.cmdShowText = new Command("Thông tin ", new IAction() {
            @Override
            public void perform() {
                ShopOnline.this.doView();
            }
        });
        Command command = this.cmdDoNothing;
        this.cmdLastLeft = command;
        this.cmdLastCenter = command;
        Scroll scroll = new Scroll();
        this.scl = scroll;
        scroll.clear();
        Scroll scroll2 = new Scroll();
        this.cmrShowTxt = scroll2;
        scroll2.clear();
        setCmdLeft();
        super.init();
        if (GCanvas.isSmartPhone) {
            this.h0 -= 10;
        }
    }

    public void doCmdSell(Vector vector) {
        if (this.isFocusMainTab) {
            return;
        }
        this.isShowText = false;
        vector.addElement(new Command("Bán", new IAction() {
            @Override
            public void perform() {
                GCanvas.inputDlg.setInfo("Nhập giá bán", new IAction() {
                    @Override
                    public void perform() {
                        try {
                            final int i = Integer.parseInt(GCanvas.inputDlg.tfInput.getText());
                            GCanvas.endDlg();
                            if (i >= 0) {
                                final ItemInInventory itemInInventory = (ItemInInventory) ((Vector) ShopOnline.ALL_ITEM.elementAt(ShopOnline.this.indexMainTile)).elementAt(ShopOnline.this.select);
                                if (!ItemInInventory.isModelClothe(itemInInventory.getTemplate().attb[9]) && itemInInventory.dayUse <= 0) {
                                    GCanvas.startYesNoDlg("Bạn có muốn đặt bán vật phẩm này với giá " + i + "  không?", new IAction() {
                                        @Override
                                        public void perform() {
                                            GameService.gI().doRequestSelItem(0, itemInInventory.itemID, i, 0);
                                            GCanvas.startOKDlg("Đã gửi thông tin bán ");
                                        }
                                    });
                                    return;
                                }
                                GCanvas.startOKDlg("Không thể bán vật phẩm này");
                                return;
                            }
                            GCanvas.startOKDlg("Không thể bán với giá này");
                        } catch (Exception unused) {
                            GCanvas.endDlg();
                            GCanvas.startOKDlg("Nhập sai,vui lòng chỉ nhập số");
                        }
                    }
                }, 3, 10, true);
                GCanvas.inputDlg.show();
            }
        }));
        vector.addElement(new Command("Bán đấu giá", new IAction() {
            @Override
            public void perform() {
                ShopOnline.this.doSell();
            }
        }));
    }

    public void doAcceptBuy(final int i, ItemInInventory itemInInventory) {
        if (itemInInventory.nameCharSell.equals(GCanvas.gameScr.mainChar.name)) {
            GCanvas.startOKDlg("Không thể mua vật phẩm do mình đăng bán");
        } else {
            GCanvas.startYesNoDlg("Bạn có muốn mua vật phẩm này không ?", new IAction() {
                @Override
                public void perform() {
                    GameService.gI().doBuyItemShopOnline(ShopOnline.this.select, i);
                    GCanvas.endDlg();
                    GCanvas.startOKDlg("Xin chờ");
                }
            });
        }
    }

    protected void doSort() {
        new Vector().addElement(new Command("Theo giá ", new IAction() {
            @Override
            public void perform() {
                ShopOnline.this.doSortBuyPrice();
            }
        }));
        new Command("Theo level", new IAction() {
            @Override
            public void perform() {
                ShopOnline.this.doSortBuyLevel();
            }
        });
    }

    protected void doView() {
        ItemInInventory itemInInventory;
        Vector vector = (Vector) ALL_ITEM.elementAt(this.indexMainTile);
        if ((!this.isFocusMainTab || GCanvas.isSmartPhone) && vector.size() != 0) {
            this.selectTxt = 0;
            int i = this.select;
            if (i < 0 || i >= vector.size()) {
                itemInInventory = null;
            } else {
                itemInInventory = (ItemInInventory) vector.elementAt(this.select);
                if (!itemInInventory.isEnoughData) {
                    itemInInventory.isEnoughData = true;
                    GameService.gI().requestItemInfo(itemInInventory.itemID, GCanvas.gameScr.mainChar.ID);
                }
            }
            String[] strArrSplitFontBStrInLine = {""};
            if (itemInInventory != null) {
                if (itemInInventory.totalKham > 0) {
                    this.arrayKhamNgoc = new byte[itemInInventory.totalKham];
                    for (int i2 = 0; i2 < itemInInventory.totalKham; i2++) {
                        this.arrayKhamNgoc[i2] = -1;
                    }
                    for (int i3 = 0; i3 < itemInInventory.allAttribute.size(); i3++) {
                        InfoAttributeItem infoAttributeItem = (InfoAttributeItem) itemInInventory.allAttribute.elementAt(i3);
                        if (infoAttributeItem.getColorPaint(false) == 3) {
                            int i4 = 0;
                            byte b = 0;
                            while (i4 < itemInInventory.numKham) {
                                this.arrayKhamNgoc[b] = Res.idNgocKham[infoAttributeItem.getID() - 10];
                                i4++;
                                b = (byte) (b + 1);
                            }
                            break;
                        }
                    }
                }
                strArrSplitFontBStrInLine = Font.normalFont[0].splitFontBStrInLine(itemInInventory.getDescription(false), this.wShowTxt - 10);
            }
            setShowTxt(strArrSplitFontBStrInLine);
        }
    }

    protected void doSell() {
        Vector vector = (Vector) ALL_ITEM.elementAt(this.indexMainTile);
        int i = this.select;
        if (i < 0 || i >= vector.size()) {
            return;
        }
        ItemInInventory itemInInventory = (ItemInInventory) vector.elementAt(this.select);
        if (ItemInInventory.isModelClothe(itemInInventory.getTemplate().attb[9]) || itemInInventory.dayUse > 0) {
            GCanvas.startOKDlg("Không thể bán vật phẩm này");
        } else {
            popupBuyItem(itemInInventory);
        }
    }

    public void paintFillLine(Graphics graphics, int i, int i2, int i3) {
        graphics.setColor(-7171438);
        graphics.fillRect(i, i2, this.w0 + 2, 1);
    }

    @Override
    public void paint(Graphics graphics) {
        Graphics graphics2 = graphics;
        Screen screen = this.lastScreen;
        if (screen != null) {
            screen.paint(graphics2);
        }
        GCanvas.resetTrans(graphics);
        Res.paintDlgDragonFull(graphics2, this.x, this.y, this.w0, this.h0);
        graphics2.setColor(-8815751);
        graphics2.fillRect(GCanvas.hw - 51, this.y + 7, 102, 22);
        graphics2.setColor(!this.isFocusMainTab ? -14408668 : -16746605);
        graphics2.fillRect(GCanvas.hw - 50, this.y + 8, 100, 20);
        Font.normalFont[0].drawString(graphics, tile[this.indexMainTile], GCanvas.w / 2, this.y + 10, 2);
        graphics2.drawImage(Res.imgBar, (GCanvas.hw - 40) + this.xRunL, this.y + 17, 3);
        graphics.drawRegion(Res.imgBar, 0, 0, 11, 7, 2, GCanvas.hw + 40 + this.xRunR, this.y + 17, 3);
        Font.smallFontWHITE.drawString(graphics, money + "$", (this.x + this.w0) - 4, (this.y + this.h0) - 12, 1);
        int i = ITEM_HEIGHT;
        int i2 = this.indexMainTile;
        if (i2 == 1) {
            int i3 = ITEM_HEIGHT + 10;
            int i4 = this.y;
            int i5 = i4 + 40;
            if (this.isSelectTypeFind) {
                this.scl.setStyle(tileFind[0].length, i3, this.x + 6, i4 + 40, this.w0, this.h0 - 40, true, 0);
                this.scl.setClip(graphics, this.x + 6, this.y + 40, this.w0 - 12, this.h0 - 55);
                for (int i6 = 0; i6 < tileFind[this.typeSearch].length; i6++) {
                    Font.normalFont[0].drawString(graphics, tileFind[this.typeSearch][i6], this.x + 6, i5 + (i6 * i3), 0);
                }
                String[][] strArr = new String[2][];
                strArr[0] = new String[]{nameItem[this.idType], this.idLevel + "", Pham[this.idPham], Color[this.idColor], this.idPlus + ""};
                String[] strArr2 = new String[1];
                strArr2[0] = this.nameChar.equals("") ? "Nhập tên" : this.nameChar;
                strArr[1] = strArr2;
                int i7 = this.idColor;
                int i8 = i7 == 1 ? 3 : i7 == 3 ? 1 : i7;
                int i9 = 0;
                while (i9 < strArr[this.typeSearch].length) {
                    int i10 = (GCanvas.w / 2) - 40;
                    int i11 = this.x;
                    short s = wStr;
                    paintNameItemSelect(graphics, i10 <= (i11 + 6) + s ? i11 + 6 + s : (GCanvas.w / 2) - 40, i5 + (i9 * i3), strArr[this.typeSearch][i9], Font.normalFont[i9 == 3 ? i8 : 0], i9);
                    i9++;
                    graphics2 = graphics2;
                }
            } else {
                paintItem(graphics2, (Vector) ALL_ITEM.elementAt(1), true);
            }
        } else {
            paintItem(graphics2, (Vector) ALL_ITEM.elementAt(i2), false);
        }
        if (this.isShowText) {
            paintShowTxt(graphics);
        }
        super.paint(graphics);
    }

    public void paintItem(Graphics graphics, Vector vector, boolean z) {
        boolean z2;
        int i = ITEM_HEIGHT * 3;
        int i2 = i + 10;
        this.scl.setStyle(vector.size(), i2, this.x + 6, this.y + 40, this.w0, this.h0 - 40, true, 0);
        this.scl.setClip(graphics, this.x + 6, this.y + 40, this.w0 - 12, this.h0 - 55);
        byte b = 0;
        boolean z3 = false;
        int i3 = 0;
        while (i3 < vector.size()) {
            ItemInInventory itemInInventory = (ItemInInventory) vector.elementAt(i3);
            int i4 = this.y + 40 + (i3 * i2);
            ItemTemplate item = Res.getItem(itemInInventory.charClazz, itemInInventory.idTemplate);
            if ((i4 + i2) - this.scl.cmy < this.y + 30 || i4 - this.scl.cmy > this.y + this.h0) {
                i2 = i2;
            } else {
                if (this.select == i3) {
                    graphics.setColor(-16742267);
                    graphics.fillRect(this.x - 1, i4 - 5, this.w0 - 2, i2);
                    z2 = true;
                } else {
                    z2 = z3;
                }
                if (itemInInventory.prizeBet > 0) {
                    graphics.drawImage(this.img, this.x + 12, i4 + 20, b);
                }
                int i5 = i4 - 5;
                paintFillLine(graphics, this.x, i5, i4);
                graphics.drawImage(Res.imgGrid, this.x + 6, i4, b);
                String[] name = itemInInventory.getName();
                int i6 = b;
                while (i6 < name.length) {
                    byte bCharAt = (byte) (name[i6].charAt(b) - '0');
                    boolean zIsDegit = WindowInfoScr.isDegit(name[i6].charAt(b));
                    if (!zIsDegit) {
                        bCharAt = b;
                    }
                    Font[] fontArr = Font.normalFont;
                    if (bCharAt >= 5) {
                        bCharAt = b;
                    }
                    fontArr[bCharAt].drawString(graphics, name[i6].substring(zIsDegit ? 1 : 0), this.x + 30, i4 + (ITEM_HEIGHT * i6) + 2, 0);
                    i6++;
                    i5 = i5;
                    item = item;
                    i4 = i4;
                    name = name;
                    b = 0;
                }
                int i7 = i5;
                GameData.paintIcon(graphics, item.idIcon, this.x + 16, i4 + 8);
                if (this.select == i3 && i3 > 1) {
                    paintLevelAndPrice(graphics, this.x + 10, i7 - (i + 15), z, true, itemInInventory);
                }
                z3 = z2;
            }
            i3++;
            i2 = i2;
            b = 0;
            z3 = z3;
        }
        int i8 = i2;
        paintFillLine(graphics, this.x, ((this.y + 40) + (vector.size() * i8)) - 5, 1);
        if (z3) {
            int i9 = this.select;
            if (i9 == 0 || i9 == 1) {
                GCanvas.resetTrans(graphics);
                paintLevelAndPrice(graphics, this.x + 10, ((this.y + 32) - i8) + (this.select * i8), z, true, (ItemInInventory) vector.elementAt(this.select));
            }
        }
    }

    public void paintShowTxt(Graphics graphics) {
        byte[] bArr;
        GCanvas.resetTrans(graphics);
        graphics.setColor(-2496);
        graphics.fillRect(this.xShowTxt - 1, this.yShowTxt - 1, this.wShowTxt + 2, this.hShowTxt + 2);
        graphics.setColor(-16751521);
        graphics.fillRect(this.xShowTxt, this.yShowTxt, this.wShowTxt, this.hShowTxt);
        this.cmrShowTxt.setStyle(this.arrInfo.length, ITEM_HEIGHT + 2, this.xShowTxt, this.yShowTxt, this.wShowTxt, this.hShowTxt, true, 0);
        this.cmrShowTxt.setClip(graphics, this.xShowTxt, this.yShowTxt, this.wShowTxt, this.hShowTxt);
        int i = 0;
        int i2 = 0;
        while (i2 < this.arrInfo.length) {
            if (i2 == 1 && (bArr = this.arrayKhamNgoc) != null && bArr.length > 0) {
                for (int i3 = 0; i3 < this.arrayKhamNgoc.length; i3++) {
                    graphics.drawImage(Res.imgKham, this.xShowTxt + 12 + (i3 * 20), this.yShowTxt + 12 + i, 3);
                    byte b = this.arrayKhamNgoc[i3];
                }
                i += 18;
            }
            int i4 = i;
            if (!this.arrInfo[i2].equals("")) {
                byte bCharAt = (byte) (this.arrInfo[i2].charAt(0) - '0');
                boolean zIsDegit = WindowInfoScr.isDegit(this.arrInfo[i2].charAt(0));
                if (!zIsDegit) {
                    bCharAt = 0;
                }
                Font[] fontArr = Font.normalFont;
                if (bCharAt >= 5) {
                    bCharAt = 0;
                }
                fontArr[bCharAt].drawString(graphics, this.arrInfo[i2].substring(zIsDegit ? 1 : 0), this.xShowTxt + 4, this.yShowTxt + 4 + ((ITEM_HEIGHT + 2) * i2) + i4, 0);
            }
            i2++;
            i = i4;
        }
    }

    @Override
    public void update() {
        Screen screen = this.lastScreen;
        if (screen != null) {
            screen.update();
        }
        int i = this.countL;
        if (i > 0) {
            this.countL = i - 1;
        }
        if (this.countL <= 0) {
            this.countL = 5;
        }
        int i2 = this.xRunL;
        if (i2 < 0) {
            this.xRunL = i2 + 1;
        }
        int i3 = this.xRunR;
        if (i3 > 0) {
            this.xRunR = i3 - 1;
        }
        if (GCanvas.currentDialog == null && !GCanvas.menu.showMenu) {
            if (this.isShowText) {
                ScrollResult scrollResultUpdateKey = this.cmrShowTxt.updateKey();
                if (scrollResultUpdateKey.isDowning || scrollResultUpdateKey.isFinish) {
                    this.selectTxt = scrollResultUpdateKey.selected;
                }
                this.cmrShowTxt.updatecm();
            } else {
                ScrollResult scrollResultUpdateKey2 = this.scl.updateKey();
                if (scrollResultUpdateKey2.isDowning || scrollResultUpdateKey2.isFinish) {
                    this.select = scrollResultUpdateKey2.selected;
                }
                if (GCanvas.isPointer(this.x, this.y + 30, this.w0, this.h0 - 30) && GCanvas.isPointerClick) {
                    GCanvas.isPointerClick = false;
                    if (scrollResultUpdateKey2.selected != -1 && this.center != null) {
                        this.center.action.perform();
                    }
                }
                this.scl.updatecm();
            }
        }
        super.update();
    }

    public void updatePoint() {
        if (this.indexMainTile == 0 && GCanvas.isPointerClick) {
            int i = (GCanvas.w / 2) - 40;
            int i2 = this.x;
            short s = wStr;
            if (GCanvas.isPointer(i <= (i2 + 6) + s ? i2 + 6 + s : (GCanvas.w / 2) - 40, this.y + 30, 100, (ITEM_HEIGHT + 10) * 4)) {
                int i3 = (GCanvas.py - (this.y + 30)) / (ITEM_HEIGHT + 10);
                GCanvas.isPointerClick = false;
                setCmdLeft();
                if (i3 >= 0 && i3 <= 3) {
                    this.select = i3;
                    if (this.center != null) {
                        this.center.action.perform();
                    }
                }
            }
        }
        if (GCanvas.isPointerClick) {
            this.isFocusMainTab = false;
            GCanvas.isPointerClick = false;
            if (GCanvas.py >= this.y && GCanvas.py <= this.y + 25) {
                if (GCanvas.px > this.x && GCanvas.px < GCanvas.w / 2) {
                    int i4 = this.indexMainTile - 1;
                    this.indexMainTile = i4;
                    if (i4 < 0) {
                        this.indexMainTile = tile.length - 1;
                    }
                    this.xRunL = -5;
                    this.scl.clear();
                    this.select = -1;
                    setCmdLeft();
                } else if (GCanvas.px < (GCanvas.w / 2) + (this.w0 / 2) && GCanvas.px > GCanvas.w / 2) {
                    int i5 = this.indexMainTile + 1;
                    this.indexMainTile = i5;
                    if (i5 > tile.length - 1) {
                        this.indexMainTile = 0;
                    }
                    this.xRunR = 5;
                    this.scl.clear();
                    this.left = this.cmdLastLeft;
                    this.select = -1;
                    setCmdLeft();
                }
            }
            if (((Vector) ALL_ITEM.elementAt(this.indexMainTile)).size() > 0 && this.select == -1) {
                this.select = 0;
            }
            setCmdLeft();
        }
    }

    @Override
    public void updateKey() {
        if (this.isFocusMainTab) {
            if (GCanvas.keyPressed[4]) {
                GCanvas.keyPressed[4] = false;
                int i = this.indexMainTile - 1;
                this.indexMainTile = i;
                if (i < 0) {
                    this.indexMainTile = tile.length - 1;
                }
                this.xRunL = -5;
                this.scl.clear();
                this.left = this.cmdLastLeft;
                this.select = -1;
                setCmdLeft();
            } else if (GCanvas.keyPressed[6]) {
                GCanvas.keyPressed[6] = false;
                int i2 = this.indexMainTile + 1;
                this.indexMainTile = i2;
                if (i2 > tile.length - 1) {
                    this.indexMainTile = 0;
                }
                this.xRunR = 5;
                this.scl.clear();
                this.left = this.cmdLastLeft;
                this.select = -1;
                setCmdLeft();
            }
            if (GCanvas.keyPressed[8]) {
                GCanvas.keyPressed[8] = false;
                this.select = 0;
                this.isFocusMainTab = false;
                setCmdLeft();
            }
        } else {
            int size = ((Vector) ALL_ITEM.elementAt(this.indexMainTile)).size();
            if (this.isSelectTypeFind && this.indexMainTile == 1) {
                if (GCanvas.keyPressed[2]) {
                    GCanvas.keyPressed[2] = false;
                    int i3 = this.select - 1;
                    this.select = i3;
                    if (i3 <= -1) {
                        this.select = -1;
                        this.isFocusMainTab = true;
                        setCmdLeft();
                    }
                    Scroll scroll = this.scl;
                    scroll.moveTo(this.select * scroll.ITEM_SIZE);
                } else if (GCanvas.keyPressed[8]) {
                    GCanvas.keyPressed[8] = false;
                    int i4 = this.select + 1;
                    this.select = i4;
                    if (i4 > tileFind[this.typeSearch].length - 1) {
                        this.select = 0;
                    }
                    Scroll scroll2 = this.scl;
                    scroll2.moveTo(this.select * scroll2.ITEM_SIZE);
                }
            } else if (!this.isFocusMainTab) {
                if (GCanvas.keyPressed[2]) {
                    GCanvas.keyPressed[2] = false;
                    if (this.isShowText) {
                        int i5 = this.selectTxt - 1;
                        this.selectTxt = i5;
                        if (i5 < 0) {
                            this.selectTxt = this.arrInfo.length - 1;
                        }
                        Scroll scroll3 = this.cmrShowTxt;
                        scroll3.moveTo(this.selectTxt * scroll3.ITEM_SIZE);
                    } else {
                        int i6 = this.select - 1;
                        this.select = i6;
                        if (i6 <= -1) {
                            this.select = -1;
                            this.isFocusMainTab = true;
                        }
                        setCmdLeft();
                        Scroll scroll4 = this.scl;
                        scroll4.moveTo(this.select * scroll4.ITEM_SIZE);
                    }
                } else if (GCanvas.keyPressed[8]) {
                    GCanvas.keyPressed[8] = false;
                    if (this.isShowText) {
                        int i7 = this.selectTxt + 1;
                        this.selectTxt = i7;
                        if (i7 > this.arrInfo.length - 1) {
                            this.selectTxt = 0;
                        }
                        Scroll scroll5 = this.cmrShowTxt;
                        scroll5.moveTo(this.selectTxt * scroll5.ITEM_SIZE);
                    } else {
                        int i8 = this.select + 1;
                        this.select = i8;
                        if (i8 > size - 1) {
                            this.select = 0;
                        }
                        setCmdLeft();
                        Scroll scroll6 = this.scl;
                        scroll6.moveTo(this.select * scroll6.ITEM_SIZE);
                    }
                }
            }
        }
        super.updateKey();
        if (GCanvas.currentDialog != null || GCanvas.menu.showMenu || this.isShowText) {
            return;
        }
        updatePoint();
    }

    public void doFind() {
        this.isSelectTypeFind = false;
        this.select = -1;
        byte b = this.typeSearch;
        if (b == 0) {
            GameService.gI().doSearchItemSell(this.idLevel, ID_TYPE_ITEM[this.idType], this.idColor, this.idPham + 1, this.idPlus);
        } else if (b == 1) {
            GameService.gI().doSearchByName(this.nameChar);
        }
    }

    public void doselect() {
        if (GCanvas.currentScreen == PageScr.gI()) {
            return;
        }
        if (this.indexMainTile == 1) {
            if (this.isSelectTypeFind) {
                Vector vector = new Vector();
                byte b = this.typeSearch;
                if (b == 0) {
                    int i = this.select;
                    int i2 = 0;
                    if (i == 0) {
                        while (i2 < nameItem.length) {
                            vector.addElement(new Command(nameItem[i2], new IAction() {
                                @Override
                                public void perform() {
                                    ShopOnline.this.idType = GCanvas.menu.selected;
                                }
                            }));
                            i2++;
                        }
                        GCanvas.menu.startAt(vector, 2);
                        return;
                    }
                    if (i == 1) {
                        GCanvas.inputDlg.setInfo("Nhập số", new IAction() {
                            @Override
                            public void perform() {
                                try {
                                    String text = GCanvas.inputDlg.tfInput.getText();
                                    ShopOnline.this.idLevel = Integer.parseInt(text);
                                    GCanvas.endDlg();
                                    if (ShopOnline.this.idLevel < 0) {
                                        GCanvas.startOKDlg("Nhập sai,vui lòng chỉ nhập số lớn hơn 0 ");
                                        ShopOnline.this.idLevel = 0;
                                    }
                                } catch (Exception unused) {
                                    GCanvas.endDlg();
                                    GCanvas.startOKDlg("Nhập sai,vui lòng chỉ nhập số");
                                }
                            }
                        }, 3, 3, true);
                        GCanvas.inputDlg.show();
                        return;
                    }
                    if (i == 2) {
                        while (i2 < Pham.length) {
                            vector.addElement(new Command(Pham[i2], new IAction() {
                                @Override
                                public void perform() {
                                    ShopOnline.this.idPham = GCanvas.menu.selected;
                                }
                            }));
                            i2++;
                        }
                        GCanvas.menu.startAt(vector, 2);
                        return;
                    }
                    if (i == 3) {
                        while (i2 < Color.length) {
                            vector.addElement(new Command(Color[i2], new IAction() {
                                @Override
                                public void perform() {
                                    ShopOnline.this.idColor = GCanvas.menu.selected;
                                }
                            }));
                            i2++;
                        }
                        GCanvas.menu.startAt(vector, 2);
                        return;
                    }
                    if (i != 4) {
                        return;
                    }
                    while (i2 < 16) {
                        vector.addElement(new Command("Cộng " + i2, new IAction() {
                            @Override
                            public void perform() {
                                ShopOnline.this.idPlus = GCanvas.menu.selected;
                            }
                        }));
                        i2++;
                    }
                    GCanvas.menu.startAt(vector, 2);
                    return;
                }
                if (b != 1 || this.select <= -1) {
                    return;
                }
                GCanvas.inputDlg.setInfo("Nhập tên", new IAction() {
                    @Override
                    public void perform() {
                        try {
                            ShopOnline.this.nameChar = GCanvas.inputDlg.tfInput.getText();
                            GCanvas.endDlg();
                            if (ShopOnline.this.nameChar.equals("")) {
                                GCanvas.startOKDlg("Tên không hợp lệ");
                            }
                        } catch (Exception unused) {
                        }
                    }
                }, 0, 25, true);
                GCanvas.inputDlg.show();
                return;
            }
            if (GCanvas.isSmartPhone) {
                Vector vector2 = new Vector();
                vector2.addElement(this.cmdShowText);
                GCanvas.menu.startAt(vector2, 2);
                return;
            }
            doView();
            return;
        }
        if (GCanvas.isSmartPhone) {
            Vector vector3 = new Vector();
            vector3.addElement(this.cmdShowText);
            GCanvas.menu.startAt(vector3, 2);
            return;
        }
        doView();
    }

    public void setItemInventory() {
        ALL_ITEM.setElementAt(ItemInInventory.getAllItemCanSell(), 3);
    }

    public boolean canBid() {
        int i = this.indexMainTile;
        if (i == 0) {
            if (((Vector) ALL_ITEM.elementAt(i)).size() > 0 && !this.isFocusMainTab && this.select < ((Vector) ALL_ITEM.elementAt(this.indexMainTile)).size() && this.select >= 0 && ((ItemInInventory) ((Vector) ALL_ITEM.elementAt(this.indexMainTile)).elementAt(this.select)).prizeBet > 0) {
                return true;
            }
        } else if (i == 1 && !this.isSelectTypeFind && ((Vector) ALL_ITEM.elementAt(i)).size() > 0 && !this.isFocusMainTab && this.select < ((Vector) ALL_ITEM.elementAt(this.indexMainTile)).size() && this.select >= 0 && ((ItemInInventory) ((Vector) ALL_ITEM.elementAt(this.indexMainTile)).elementAt(this.select)).prizeBet > 0) {
            return true;
        }
        return false;
    }

    public void setCmdLeft() {
        if (this.indexMainTile == 3) {
            setItemInventory();
        }
        int i = this.indexMainTile;
        if (i == 0) {
            if (!canBid()) {
                if (this.isFocusMainTab) {
                    this.left = new Command("", new IAction() {
                        @Override
                        public void perform() {
                        }
                    });
                    return;
                } else {
                    if (((Vector) ALL_ITEM.elementAt(this.indexMainTile)).size() > 0) {
                        this.left = new Command("Mua", new IAction() {
                            @Override
                            public void perform() {
                                if (ShopOnline.this.select < 0) {
                                    return;
                                }
                                ShopOnline.this.doAcceptBuy(0, (ItemInInventory) ((Vector) ShopOnline.ALL_ITEM.elementAt(ShopOnline.this.indexMainTile)).elementAt(ShopOnline.this.select));
                            }
                        });
                        return;
                    }
                    return;
                }
            }
            this.left = new Command("Menu", new AnonymousClass22());
            return;
        }
        if (i == 1) {
            this.left = new Command("Menu", new AnonymousClass23());
            return;
        }
        if (i == 2) {
            this.left = this.cmdDoNothing;
            return;
        }
        if (i == 3) {
            this.left = this.cmdSell;
            return;
        }
        if (i == 4) {
            final Vector vector = (Vector) ALL_ITEM.elementAt(i);
            if (vector.size() > 0 || money > 0) {
                this.left = new Command("Menu", new IAction() {
                    @Override
                    public void perform() {
                        ShopOnline.this.isShowText = false;
                        Vector vector2 = new Vector();
                        if (ShopOnline.money > 0) {
                            vector2.addElement(new Command("Rút tiền", new IAction() {
                                @Override
                                public void perform() {
                                    GameService.gI().doGetMoneySellInventory();
                                }
                            }));
                        }
                        if (vector.size() > 0 && ShopOnline.this.select >= 0) {
                            vector2.addElement(new Command("Bỏ vào hành trang", new IAction() {
                                @Override
                                public void perform() {
                                    GameService.gI().doGetItemBackInventory(ShopOnline.this.select);
                                }
                            }));
                        }
                        GCanvas.menu.startAt(vector2, 2);
                    }
                });
                return;
            } else {
                this.left = new Command("", new IAction() {
                    @Override
                    public void perform() {
                    }
                });
                return;
            }
        }
        if (i == 5) {
            if (((Vector) ALL_ITEM.elementAt(i)).size() == 0 || this.isFocusMainTab) {
                this.left = new Command("", new IAction() {
                    @Override
                    public void perform() {
                    }
                });
            } else {
                this.left = new Command("Menu", new AnonymousClass27());
            }
        }
    }

    class AnonymousClass22 implements IAction {
        AnonymousClass22() {
        }

        @Override
        public void perform() {
            ShopOnline.this.isShowText = false;
            Vector vector = new Vector();
            vector.addElement(new Command("Mua", new IAction() {
                @Override
                public void perform() {
                    if (ShopOnline.this.select < 0) {
                        return;
                    }
                    ShopOnline.this.doAcceptBuy(0, (ItemInInventory) ((Vector) ShopOnline.ALL_ITEM.elementAt(ShopOnline.this.indexMainTile)).elementAt(ShopOnline.this.select));
                }
            }));
            vector.addElement(new Command("Bid", new AnonymousClass2()));
            GCanvas.menu.startAt(vector, 0);
        }

        class AnonymousClass2 implements IAction {
            AnonymousClass2() {
            }

            @Override
            public void perform() {
                if (ShopOnline.this.select < 0) {
                    return;
                }
                final ItemInInventory itemInInventory = (ItemInInventory) ((Vector) ShopOnline.ALL_ITEM.elementAt(ShopOnline.this.indexMainTile)).elementAt(ShopOnline.this.select);
                GCanvas.inputDlg.setInfo("Nhập giá", new IAction() {
                    @Override
                    public void perform() {
                        try {
                            final int i = Integer.parseInt(GCanvas.inputDlg.tfInput.getText());
                            GCanvas.endDlg();
                            if (i < 0 || i <= itemInInventory.prizeBet || i >= itemInInventory.prize) {
                                GCanvas.startOKDlg("Giá bid phải lớn hơn " + itemInInventory.prizeBet + " và nhỏ hơn " + itemInInventory.prize);
                            } else {
                                GCanvas.startYesNoDlg("Bạn có muốn đặt bid cho " + itemInInventory.getTemplate().name + " với giá " + i + " không?", new IAction() {
                                    @Override
                                    public void perform() {
                                        if (ShopOnline.this.select < 0) {
                                            return;
                                        }
                                        GameService.gI().doBid(ShopOnline.this.select, 0, i);
                                        GCanvas.endDlg();
                                    }
                                });
                            }
                        } catch (Exception unused) {
                            GCanvas.endDlg();
                            GCanvas.startOKDlg("Nhập sai,vui lòng chỉ nhập số");
                        }
                    }
                }, 3, 10, true);
                GCanvas.inputDlg.show();
            }
        }
    }

    class AnonymousClass23 implements IAction {
        AnonymousClass23() {
        }

        @Override
        public void perform() {
            ShopOnline.this.isShowText = false;
            Vector vector = new Vector();
            if (!ShopOnline.this.isFocusMainTab && ((Vector) ShopOnline.ALL_ITEM.elementAt(ShopOnline.this.indexMainTile)).size() > 0 && ShopOnline.this.select > -1) {
                if (ShopOnline.this.select < 0) {
                    return;
                }
                final ItemInInventory itemInInventory = (ItemInInventory) ((Vector) ShopOnline.ALL_ITEM.elementAt(ShopOnline.this.indexMainTile)).elementAt(ShopOnline.this.select);
                if (!itemInInventory.isMyItem()) {
                    vector.addElement(new Command("Mua", new IAction() {
                        @Override
                        public void perform() {
                            GCanvas.startYesNoDlg("Bạn có muốn mua " + itemInInventory.getTemplate().name + " với giá " + itemInInventory.prize + " không?", new IAction() {
                                @Override
                                public void perform() {
                                    if (ShopOnline.this.select < 0) {
                                        return;
                                    }
                                    ShopOnline.this.doAcceptBuy(1, (ItemInInventory) ((Vector) ShopOnline.ALL_ITEM.elementAt(ShopOnline.this.indexMainTile)).elementAt(ShopOnline.this.select));
                                }
                            });
                        }
                    }));
                    if (ShopOnline.this.canBid()) {
                        vector.addElement(new Command("Bid", new AnonymousClass2(itemInInventory)));
                    }
                }
            }
            if (ShopOnline.this.isSelectTypeFind) {
                vector.addElement(new Command("Tìm", new IAction() {
                    @Override
                    public void perform() {
                        ShopOnline.this.doFind();
                    }
                }));
            }
            if (ShopOnline.this.typeSearch != 0) {
                vector.addElement(new Command("Tìm theo loại", new IAction() {
                    @Override
                    public void perform() {
                        ShopOnline.this.reset();
                        ShopOnline.this.typeSearch = (byte) 0;
                    }
                }));
            }
            if (ShopOnline.this.typeSearch != 1) {
                vector.addElement(new Command("Tìm theo người bán", new IAction() {
                    @Override
                    public void perform() {
                        ShopOnline.this.reset();
                        ShopOnline.this.typeSearch = (byte) 1;
                    }
                }));
            }
            if (((Vector) ShopOnline.ALL_ITEM.elementAt(ShopOnline.this.indexMainTile)).size() > 0) {
                vector.addElement(new Command("Kết quả", new IAction() {
                    @Override
                    public void perform() {
                        ShopOnline.this.isSelectTypeFind = false;
                        ShopOnline.this.select = -1;
                        ShopOnline.this.setCmdLeft();
                    }
                }));
            }
            if (ShopOnline.nPage[ShopOnline.this.indexMainTile] > 0) {
                if (ShopOnline.indexPage[ShopOnline.this.indexMainTile] < ShopOnline.nPage[ShopOnline.this.indexMainTile] - 1) {
                    vector.addElement(new Command("Trang sau", new IAction() {
                        @Override
                        public void perform() {
                            short[] sArr = ShopOnline.indexPage;
                            int i = ShopOnline.this.indexMainTile;
                            sArr[i] = (short) (sArr[i] + 1);
                        }
                    }));
                }
                if (ShopOnline.indexPage[ShopOnline.this.indexMainTile] > 0) {
                    vector.addElement(new Command("Trang trước", new IAction() {
                        @Override
                        public void perform() {
                            short[] sArr = ShopOnline.indexPage;
                            int i = ShopOnline.this.indexMainTile;
                            sArr[i] = (short) (sArr[i] - 1);
                        }
                    }));
                }
            }
            GCanvas.menu.startAt(vector, 0);
        }

        class AnonymousClass2 implements IAction {
            final ItemInInventory val$item;

            AnonymousClass2(ItemInInventory itemInInventory) {
                this.val$item = itemInInventory;
            }

            @Override
            public void perform() {
                GCanvas.inputDlg.setInfo("Nhập giá", new IAction() {
                    @Override
                    public void perform() {
                        try {
                            final int i = Integer.parseInt(GCanvas.inputDlg.tfInput.getText());
                            GCanvas.endDlg();
                            if (i < 0 || i <= AnonymousClass2.this.val$item.prizeBet || i >= AnonymousClass2.this.val$item.prize) {
                                GCanvas.startOKDlg("Giá bid phải lớn hơn " + AnonymousClass2.this.val$item.prizeBet + " và nhỏ hơn " + AnonymousClass2.this.val$item.prize);
                            } else {
                                GCanvas.startYesNoDlg("Bạn có muốn đặt bid cho " + AnonymousClass2.this.val$item.getTemplate().name + " với giá " + i + " không?", new IAction() {
                                    @Override
                                    public void perform() {
                                        if (ShopOnline.this.select < 0) {
                                            return;
                                        }
                                        GameService.gI().doBid(ShopOnline.this.select, 1, i);
                                        GCanvas.endDlg();
                                    }
                                });
                            }
                        } catch (Exception unused) {
                            GCanvas.endDlg();
                            GCanvas.startOKDlg("Nhập sai,vui lòng chỉ nhập số");
                        }
                    }
                }, 3, 10, true);
                GCanvas.inputDlg.show();
            }
        }
    }

    class AnonymousClass27 implements IAction {
        AnonymousClass27() {
        }

        @Override
        public void perform() {
            ShopOnline.this.isShowText = false;
            Vector vector = new Vector();
            vector.addElement(new Command("Mua", new IAction() {
                @Override
                public void perform() {
                    ShopOnline.this.doAcceptBuy(2, (ItemInInventory) ((Vector) ShopOnline.ALL_ITEM.elementAt(ShopOnline.this.indexMainTile)).elementAt(ShopOnline.this.select));
                }
            }));
            vector.addElement(new Command("Đặt bid", new AnonymousClass2()));
            GCanvas.menu.startAt(vector, 0);
        }

        class AnonymousClass2 implements IAction {
            AnonymousClass2() {
            }

            @Override
            public void perform() {
                final ItemInInventory itemInInventory = (ItemInInventory) ((Vector) ShopOnline.ALL_ITEM.elementAt(ShopOnline.this.indexMainTile)).elementAt(ShopOnline.this.select);
                GCanvas.inputDlg.setInfo("Nhập giá", new IAction() {
                    @Override
                    public void perform() {
                        try {
                            final int i = Integer.parseInt(GCanvas.inputDlg.tfInput.getText());
                            GCanvas.endDlg();
                            if (i < 0 || i <= itemInInventory.prizeBet || i >= itemInInventory.prize) {
                                GCanvas.startOKDlg("Giá bid phải lớn hơn " + itemInInventory.prizeBet + " và nhỏ hơn " + itemInInventory.prize);
                            } else {
                                GCanvas.startYesNoDlg("Bạn có muốn đặt bid cho " + itemInInventory.getTemplate().name + " với giá " + i + " không?", new IAction() {
                                    @Override
                                    public void perform() {
                                        GameService.gI().doBid(ShopOnline.this.select, 2, i);
                                        GCanvas.endDlg();
                                    }
                                });
                            }
                        } catch (Exception unused) {
                            GCanvas.endDlg();
                            GCanvas.startOKDlg("Nhập sai,vui lòng chỉ nhập số");
                        }
                    }
                }, 3, 10, true);
                GCanvas.inputDlg.show();
            }
        }
    }

    public static void popupBuyItem(final ItemInInventory itemInInventory) {
        final TField tField = new TField(GCanvas.hw - 60, (GCanvas.h - deltaY) - 80, 120, deltaY);
        tField.setIputType(3);
        tField.isFocus = true;
        tField.setText("100000");
        PageScr.gI().right = tField.cmdClear;
        final TField tField2 = new TField(GCanvas.hw - 60, (GCanvas.h - deltaY) - 45, 120, deltaY);
        tField2.setIputType(3);
        tField2.setText("10000");
        PageScr.gI().setInfo(GCanvas.hw - 70, (GCanvas.h - deltaY) - 120, 140, 100, "NHẬP GIÁ BÁN", new Command("Bán", new IAction() {
            @Override
            public void perform() {
                try {
                    final int i = Integer.parseInt(tField.getText().trim());
                    final int i2 = Integer.parseInt(tField2.getText().trim());
                    if (i < i2) {
                        GCanvas.startOKDlg("Giá bid phải nhỏ hơn giá bán");
                    } else {
                        GCanvas.startYesNoDlg("Bạn có muốn đặt bán vật phẩm này với giá " + tField.getText().trim() + "  không?", new IAction() {
                            @Override
                            public void perform() {
                                GameService.gI().doRequestSelItem(0, itemInInventory.itemID, i, i2);
                                PageScr.gI().left.action.perform();
                                GCanvas.startOKDlg("Đã gửi thông tin bán ");
                            }
                        });
                    }
                } catch (Exception unused) {
                    GCanvas.startOKDlg("Giá bán không hợp lệ");
                }
            }
        }));
        PageScr.gI().layer = new Layer() {
            @Override
            public void update() {
                if (GCanvas.isKeyPressed(2) || GCanvas.isKeyPressed(8)) {
                    if (tField.isFocus) {
                        tField.isFocus = false;
                        tField2.isFocus = true;
                        PageScr.gI().right = tField2.cmdClear;
                    } else {
                        tField.isFocus = true;
                        tField2.isFocus = false;
                        PageScr.gI().right = tField.cmdClear;
                    }
                }
                tField.update();
                tField2.update();
            }

            @Override
            public void keyPress(int i) {
                tField.keyPressed(i);
                tField2.keyPressed(i);
            }

            @Override
            public void paint(Graphics graphics, int i, int i2) {
                Font.normalFont[0].drawString(graphics, "Giá bán", GCanvas.hw - 60, (GCanvas.h - Screen.deltaY) - 94, 0);
                Font.normalFont[0].drawString(graphics, "Giá bid", GCanvas.hw - 60, (GCanvas.h - Screen.deltaY) - 59, 0);
                tField.paint(graphics);
                graphics.setClip(GCanvas.hw - 70, (GCanvas.h - Screen.deltaY) - 120, 140, 100);
                graphics.ClipRec(GCanvas.hw - 70, (GCanvas.h - Screen.deltaY) - 120, 140, 100);
                tField2.paint(graphics);
                graphics.restoreCanvas();
            }
        };
        PageScr.gI().switchToMe();
    }

    public void onItemInfo() {
        if (this.isShowText) {
            doView();
        }
    }
}
