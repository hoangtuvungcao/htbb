package game.render.screen;

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
import game.render.Canvas;
import game.render.Res;
import java.util.Vector;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

public class ShopOnline extends Screen {
    /** 31/10/2026: đang chạy updateKey không chạm thô (ManCu - vùng chạm kiểu chung đã xử lý) */
    boolean boc_updateKey;

    public Scroll scl;
    public Scroll cmrShowTxt;
    public static short level;
    public static short wStr;
    public int select;
    public int selectTxt;
    public int x;
    public int y;
    public int w0;
    public int h0;
    public int xShowTxt;
    public int yShowTxt;
    public int wShowTxt;
    public int hShowTxt;
    private int countL;
    private int indexMainTile;
    private int idType;
    private int idPham;
    private int idColor;
    public Command cmdSell;
    public Command cmdShowText;
    public Screen lastScreen;
    private boolean isFocusMainTab;
    public boolean isShowText;
    private int xRunL;
    private int xRunR;
    private byte[] arrayKhamNgoc;
    public static Vector ALL_ITEM = new Vector();
    public static String[] tile = {"Chợ", "Tìm", "Đang bán", "Hành trang", "Kho", "Đấu giá"};
    public static short[] nPage = new short[6];
    public static short[] indexPage = new short[6];
    public static String[][] tileFind = {new String[]{"Loại:", "Cấp:", "Phẩm:", "Màu:", "Cộng:"}, new String[]{"N.vật"}, new String[0]};
    public static String[] nameItem = {"Áo", "Quần", "Nón", "Than lửa", "Hạt giống thần", "Bút", "Búa", "Cung", "Nhẫn", "Dây chuyền", "Giày", "Găng tay", "Phiến đá hệ", "Giáp thú cưỡi", "Vòng chân", "Nón thú cưỡi", "Bàn đạp", "Yên cương", "Ruy băng"};
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
    Image img = null;
    public int wShowLv = 110;
    public String price = "10.000.000.000";
    public String strLv = "";
    public Command cmdDoNothing = new Command("", new IAction() {
        @Override
        public void perform() {
        }
    });

    public void reset() {
        this.select = -1;
        this.selectTxt = 0;
        level = (short) 0;
        pham = "";
        mau = "";
        this.indexMainTile = 1;
        this.isSelectTypeFind = true;
        this.isFocusMainTab = !Canvas.isSmartPhone;
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

    public void setShowTxt(String[] str) {
        this.arrInfo = new String[]{""};
        this.arrInfo = str;
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

    public void paintNameItemSelect(Graphics g, int x, int y, String str, Font f, int id) {
        // pokemon: mũi tên vàng chỉ vào tên (thay ảnh mũi tên kiếm hiệp)
        game.pokemon.GiaoDien.muiTab(g, x + 4 - (this.select == id ? this.countL : 0), y + 7, false);
        game.pokemon.GiaoDien.muiTab(g, x + 96 + (this.select == id ? this.countL : 0), y + 7, true);
        f.drawString(g, str, x + (100 / 2), y, 2);
    }

    @Override
    public void switchToMe() {
        init();
        super.switchToMe();
    }

    @Override
    public void switchToMe(Screen lastSCR) {
        this.lastScreen = lastSCR;
        init();
        super.switchToMe(lastSCR);
    }

    @Override
    public void init() {
        if (Canvas.isSmartPhone) {
            tileFind[1][0] = "Pokémon";
        }
        if (this.img == null) {
            try {
                this.img = Image.createImage("/0.png");
            } catch (Exception e) {
            }
        }
        int ww = Canvas.w - 6;
        int hh = Canvas.h - 30;
        if (ww > 320) {
            ww = 320;
        }
        if (hh > 240) {
            hh = 240;
        }
        this.w0 = ww;
        this.h0 = hh;
        this.x = (Canvas.w / 2) - (this.w0 / 2);
        this.y = ((Canvas.h / 2) - (this.h0 / 2)) - 10;
        if (this.y < 8) {
            this.y = 8;
        }
        this.wShowTxt = Canvas.w - 40;
        if (this.wShowTxt > 200) {
            this.wShowTxt = 200;
        }
        this.hShowTxt = Canvas.h - 80;
        if (this.hShowTxt > 180) {
            this.hShowTxt = 180;
        }
        this.xShowTxt = (Canvas.w / 2) - (this.wShowTxt / 2);
        this.yShowTxt = (Canvas.h / 2) - (this.hShowTxt / 2);
        if (this.yShowTxt < 5) {
            this.yShowTxt = 5;
        }
        for (int i = 0; i < tileFind[0].length; i++) {
            if (wStr < Font.normalFont[0].getWidth(tileFind[0][i])) {
                wStr = (short) Font.normalFont[0].getWidth(tileFind[0][i]);
            }
        }
        wStr = (short) (wStr + 5);
        this.select = -1;
        this.isFocusMainTab = !Canvas.isSmartPhone;
        this.isShowText = false;
        this.idType = 0;
        this.idPham = 0;
        this.idColor = 0;
        this.idLevel = 1;
        this.center = new Command(Canvas.isSmartPhone ? "" : "Chọn", new IAction() {
            @Override
            public void perform() {
                ShopOnline.this.doselect();
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
        this.cmdSell = new Command("Menu", new IAction() {
            @Override
            public void perform() {
                Vector menu = new Vector();
                ShopOnline.this.doCmdSell(menu);
                Canvas.menu.startAt(menu, 0);
            }
        });
        this.cmdShowText = new Command("Thông tin ", new IAction() {
            @Override
            public void perform() {
                ShopOnline.this.doView();
            }
        });
        this.scl = new Scroll();
        this.scl.clear();
        this.cmrShowTxt = new Scroll();
        this.cmrShowTxt.clear();
        setCmdLeft();
        super.init();
        if (Canvas.isSmartPhone) {
            setCmdLeft();
            this.h0 -= 10;
        }
    }

    public void doCmdSell(Vector menu) {
        if (this.isFocusMainTab) {
            return;
        }
        this.isShowText = false;
        menu.addElement(new Command("Bán", new AnonymousClass6()));
        menu.addElement(new Command("Bán đấu giá", new IAction() {
            @Override
            public void perform() {
                ShopOnline.this.doSell();
            }
        }));
    }

    class AnonymousClass6 implements IAction {
        AnonymousClass6() {
        }

        @Override
        public void perform() {
            Canvas.inputDlg.setInfo("Nhập giá bán", new IAction() {
                @Override
                public void perform() {
                    try {
                        String a = Canvas.inputDlg.tfInput.getText();
                        final int price = Integer.parseInt(a);
                        Canvas.endDlg();
                        if (price >= 0) {
                            final ItemInInventory item = (ItemInInventory) ((Vector) ShopOnline.ALL_ITEM.elementAt(ShopOnline.this.indexMainTile)).elementAt(ShopOnline.this.select);
                            if (ItemInInventory.isModelClothe(item.getTemplate().attb[9]) || item.dayUse > 0) {
                                Canvas.startOKDlg("Không thể bán vật phẩm này");
                                return;
                            } else {
                                Canvas.startYesNoDlg(new StringBuffer("Bạn có muốn đặt bán vật phẩm này với giá ").append(price).append("  không?").toString(), new IAction() {
                                    @Override
                                    public void perform() {
                                        GameService.gI().doRequestSelItem(0, item.itemID, price, 0);
                                        Canvas.startOKDlg("Đã gửi thông tin bán ");
                                    }
                                });
                                return;
                            }
                        }
                        Canvas.startOKDlg("Không thể bán với giá này");
                    } catch (Exception e) {
                        Canvas.endDlg();
                        Canvas.startOKDlg("Nhập sai,vui lòng chỉ nhập số");
                    }
                }
            }, 3, 10, true);
            Canvas.inputDlg.show();
        }
    }

    public void doAcceptBuy(final int new_search, ItemInInventory item) {
        if (item.nameCharSell.equals(Canvas.gameScr.mainChar.name)) {
            Canvas.startOKDlg("Không thể mua vật phẩm do mình đăng bán");
        } else {
            Canvas.startYesNoDlg("Bạn có muốn mua vật phẩm này không ?", new IAction() {
                @Override
                public void perform() {
                    GameService.gI().doBuyItemShopOnline(ShopOnline.this.select, new_search);
                    Canvas.endDlg();
                    Canvas.startOKDlg("Xin chờ");
                }
            });
        }
    }

    protected void doSort() {
        Vector m = new Vector();
        Command cmd = new Command("Theo giá ", new IAction() {
            @Override
            public void perform() {
                ShopOnline.this.doSortBuyPrice();
            }
        });
        m.addElement(cmd);
        new Command("Theo cấp", new IAction() {
            @Override
            public void perform() {
                ShopOnline.this.doSortBuyLevel();
            }
        });
    }

    protected void doSortBuyLevel() {
    }

    protected void doSortBuyPrice() {
    }

    protected void doView() {
        Vector allItem = (Vector) ALL_ITEM.elementAt(this.indexMainTile);
        if ((this.isFocusMainTab && !Canvas.isSmartPhone) || allItem.size() == 0) {
            return;
        }
        this.selectTxt = 0;
        ItemInInventory item = null;
        if (this.select >= 0 && this.select < allItem.size()) {
            item = (ItemInInventory) allItem.elementAt(this.select);
            if (!item.isEnoughData) {
                item.isEnoughData = true;
                GameService.gI().requestItemInfo(item.itemID, Canvas.gameScr.mainChar.ID);
            }
        }
        String[] a = {""};
        if (item != null) {
            if (item.totalKham > 0) {
                this.arrayKhamNgoc = new byte[item.totalKham];
                for (int i = 0; i < item.totalKham; i++) {
                    this.arrayKhamNgoc[i] = -1;
                }
                byte k = 0;
                for (int i2 = 0; i2 < item.allAttribute.size(); i2++) {
                    InfoAttributeItem name = (InfoAttributeItem) item.allAttribute.elementAt(i2);
                    if (name.getColorPaint(false) == 3) {
                        for (int j = 0; j < item.numKham; j++) {
                            byte b = k;
                            k = (byte) (b + 1);
                            this.arrayKhamNgoc[b] = Res.idNgocKham[name.getID() - 10];
                        }
                        break;
                    }
                }
            }
            a = Font.normalFont[0].splitFontBStrInLine(item.getDescription(false), this.wShowTxt - 10);
        }
        setShowTxt(a);
    }

    protected void doSell() {
        Vector allItem = (Vector) ALL_ITEM.elementAt(this.indexMainTile);
        if (this.select >= 0 && this.select < allItem.size()) {
            ItemInInventory item = (ItemInInventory) allItem.elementAt(this.select);
            if (ItemInInventory.isModelClothe(item.getTemplate().attb[9]) || item.dayUse > 0) {
                Canvas.startOKDlg("Không thể bán vật phẩm này");
            } else {
                popupBuyItem(item);
            }
        }
    }

    public void doAcceptSell() {
    }

    public void paintFillLine(Graphics g, int x, int y1, int y2) {
        g.setColor(game.pokemon.GiaoDien.XANH_SANG);
        g.fillRect(x, y1, this.w0 + 2, 1);
    }

    static String boMau(String t) {
        return t != null && t.length() > 1 && WindowInfoScr.isDegit(t.charAt(0)) ? t.substring(1) : t;
    }

    /**
     * 31/10/2026 (soi giao diện: chợ còn hộp J2ME cũ - mũi tên tab, chữ nhỏ, không vùng chạm): máy có con trỏ - tấm chung,
     * chip 6 tab, hàng món (icon / tên / giá) chạm = chọn rồi Chọn, bộ lọc Tìm = hàng chạm được, nút Mua / Menu / Thông tin
     * trong tấm, xu đang có ở góc. Việc / gói gửi như cũ.
     */
    void veCham(Graphics g) {
        this.anNutLenh = true;
        if (this.isShowText) {
            int[] c = game.pokemon.ManCu.tam(g, this, "Thông tin món", game.pokemon.ManCu.viec(this.right), null);
            game.pokemon.ManCu.vanBan(g, c, this.arrInfo, null);
            return;
        }
        Vector v = (Vector) ALL_ITEM.elementAt(this.indexMainTile);
        boolean coMon = this.select >= 0 && this.select < v.size() && !(this.indexMainTile == 1 && this.isSelectTypeFind);
        int[] c = game.pokemon.ManCu.tam(g, this, "Chợ", game.pokemon.ManCu.viec(this.right),
                new Command[]{coMon ? this.cmdShowText : null, this.left});
        game.pokemon.ManCu.tiepQuanMenu("Chợ");
        int k = game.pokemon.Bang.cach();
        Object[] vc = new Object[tile.length];
        for (int i = 0; i < tile.length; i++) {
            final int t = i;
            vc[i] = new IAction() {
                public void perform() {
                    ShopOnline.this.indexMainTile = t;
                    ShopOnline.this.scl.clear();
                    ShopOnline.this.select = -1;
                    ShopOnline.this.isFocusMainTab = false;
                    ShopOnline.this.setCmdLeft();
                }
            };
        }
        int hc = game.pokemon.Bang.hangChip(g, c[0], c[1], c[2], tile, this.indexMainTile, vc) + k;
        c[1] += hc;
        c[3] -= hc;
        // xu đang có (chạm = chú thích) - hàng dưới cùng
        int hx = Math.max(game.pokemon.VungCham.MIN, game.pokemon.ChuDe.cao(game.pokemon.ChuDe.PHU) + 4);
        game.pokemon.TienTe.veGiaCham(g, game.pokemon.TienTe.XU, money, c[0] + 4, c[1] + c[3] - hx + (hx - Font.arialFont.getHeight()) / 2, Font.arialFont, true);
        c[3] -= hx + k;
        if (this.indexMainTile == 1 && this.isSelectTypeFind) {
            final String[] nhan = tileFind[this.typeSearch];
            final String[] gt = this.typeSearch == 0 ? new String[]{nameItem[this.idType], String.valueOf(this.idLevel), Pham[this.idPham], Color[this.idColor],
                String.valueOf(this.idPlus)} : new String[]{this.nameChar.equals("") ? "Nhập tên" : this.nameChar};
            game.pokemon.ManCu.danhSach(g, c, nhan.length, this.select, new game.pokemon.ManCu.Dong() {
                public String chinh(int i) {
                    return nhan[i] + " " + (i < gt.length ? gt[i] : "");
                }

                public String phu(int i) {
                    return null;
                }

                public void cham(int i) {
                    ShopOnline.this.select = i;
                    ShopOnline.this.doselect();
                }
            }, null);
            return;
        }
        final Vector l = v;
        final boolean mua = this.indexMainTile <= 1;
        game.pokemon.ManCu.danhSach(g, c, l.size(), this.select, new game.pokemon.ManCu.Dong() {
            public String chinh(int i) {
                String[] n = ((ItemInInventory) l.elementAt(i)).getName();
                return n == null || n.length == 0 ? "?" : boMau(n[0]);
            }

            public String phu(int i) {
                ItemInInventory it = (ItemInInventory) l.elementAt(i);
                String[] n = it.getName();
                String gia = it.prize > 0 ? "Giá " + game.pokemon.TienTe.gon(it.prize) + " xu" : null;
                String dg = it.prizeBet > 0 ? "Đấu giá " + game.pokemon.TienTe.gon(it.prizeBet) + " xu" : null;
                String p = gia != null && dg != null ? gia + " - " + dg : gia != null ? gia : dg;
                if (p == null && n != null && n.length > 1) {
                    p = boMau(n[1]);
                }
                return p == null ? (mua ? "" : null) : p;
            }

            public void cham(int i) {
                ShopOnline.this.select = i;
                ShopOnline.this.isFocusMainTab = false;
                ShopOnline.this.setCmdLeft();
            }
        }, this.indexMainTile == 1 ? "Chưa có kết quả - bấm Tìm" : "Chưa có món nào");
    }

    public void paint(Graphics g) {
        if (this.lastScreen != null) {
            this.lastScreen.paint(g);
        }
        Canvas.resetTrans(g);
        if (game.pokemon.ManCu.bat()) {
            veCham(g);
            return;
        }
        Res.paintDlgDragonFull(g, this.x, this.y, this.w0, this.h0);
        // pokemon: viên tên tab + mũi tên vàng (thay thanh xám + mũi tên ảnh kiếm hiệp)
        game.pokemon.GiaoDien.vienTab(g, Canvas.hw - 51, this.y + 7, 102, 21, this.isFocusMainTab);
        Font.normalFont[0].drawString(g, tile[this.indexMainTile], Canvas.w / 2, this.y + 10, 2);
        game.pokemon.GiaoDien.muiTab(g, (Canvas.hw - 64) + this.xRunL, this.y + 17, true);
        game.pokemon.GiaoDien.muiTab(g, Canvas.hw + 64 + this.xRunR, this.y + 17, false);
        {   // pokemon: biểu tượng xu thay "$"; chạm = chú thích
            int wTien = game.pokemon.TienTe.rongGia(game.pokemon.TienTe.XU, money, Font.smallFontColor[0], true);
            game.pokemon.TienTe.veGiaCham(g, game.pokemon.TienTe.XU, money, (this.x + this.w0) - 4 - wTien, (this.y + this.h0) - 14, Font.smallFontColor[0], true);
        }
        int i = Screen.ITEM_HEIGHT + 10;
        if (this.indexMainTile == 1) {
            int kc = Screen.ITEM_HEIGHT + 10;
            int yBegin = this.y + 40;
            if (this.isSelectTypeFind) {
                this.scl.setStyle(tileFind[0].length, kc, this.x + 6, this.y + 40, this.w0, this.h0 - 40, true, 0);
                this.scl.setClip(g, this.x + 6, this.y + 40, this.w0 - 12, this.h0 - 55);
                for (int i2 = 0; i2 < tileFind[this.typeSearch].length; i2++) {
                    Font.normalFont[0].drawString(g, tileFind[this.typeSearch][i2], this.x + 6, yBegin + (i2 * kc), 0);
                }
                String[][] info = new String[2][];
                info[0] = new String[]{nameItem[this.idType], new StringBuffer(String.valueOf(this.idLevel)).toString(), Pham[this.idPham], Color[this.idColor], new StringBuffer(String.valueOf(this.idPlus)).toString()};
                String[] strArr = new String[1];
                strArr[0] = this.nameChar.equals("") ? "Nhập tên" : this.nameChar;
                info[1] = strArr;
                int id = this.idColor;
                if (this.idColor == 1) {
                    id = 3;
                } else if (this.idColor == 3) {
                    id = 1;
                }
                int i3 = 0;
                while (i3 < info[this.typeSearch].length) {
                    paintNameItemSelect(g, (Canvas.w / 2) - 40 <= (this.x + 6) + wStr ? this.x + 6 + wStr : (Canvas.w / 2) - 40, yBegin + (i3 * kc), info[this.typeSearch][i3], Font.normalFont[i3 == 3 ? id : 0], i3);
                    i3++;
                }
            } else {
                paintItem(g, (Vector) ALL_ITEM.elementAt(1), true);
            }
        } else {
            paintItem(g, (Vector) ALL_ITEM.elementAt(this.indexMainTile), false);
        }
        if (this.isShowText) {
            paintShowTxt(g);
        }
        super.paint(g);
    }

    public void paintItem(Graphics g, Vector vt, boolean isShopBuy) {
        int kc = (Screen.ITEM_HEIGHT * 3) + 10;
        this.scl.setStyle(vt.size(), kc, this.x + 6, this.y + 40, this.w0, this.h0 - 40, true, 0);
        this.scl.setClip(g, this.x + 6, this.y + 40, this.w0 - 12, this.h0 - 55);
        boolean isOne = false;
        for (int i = 0; i < vt.size(); i++) {
            ItemInInventory item = (ItemInInventory) vt.elementAt(i);
            int yy = this.y + 40 + (i * kc);
            ItemTemplate it = Res.getItem(item.charClazz, item.idTemplate);
            if ((yy + kc) - this.scl.cmy >= this.y + 30 && yy - this.scl.cmy <= this.y + this.h0) {
                if (this.select == i) {
                    g.setColor(game.pokemon.GiaoDien.XANH);
                    g.fillRect(this.x - 1, yy - 5, this.w0 - 2, kc);
                    isOne = true;
                }
                if (item.prizeBet > 0) {
                    g.drawImage(this.img, this.x + 12, yy + 20, 0);
                }
                paintFillLine(g, this.x, yy - 5, yy);
                g.drawImage(Res.imgGrid, this.x + 6, yy, 0);
                String[] name = item.getName();
                for (int j = 0; j < name.length; j++) {
                    byte color = (byte) (name[j].charAt(0) - '0');
                    byte indexsub = 1;
                    if (!WindowInfoScr.isDegit(name[j].charAt(0))) {
                        color = 0;
                        indexsub = 0;
                    }
                    Font.normalFont[color >= 5 ? (byte) 0 : color].drawString(g, name[j].substring(indexsub), 30 + this.x, yy + (j * Screen.ITEM_HEIGHT) + 2, 0);
                }
                GameData.paintIcon(g, it.idIcon, 16 + this.x, yy + 8);
                if (this.select == i && i > 1) {
                    paintLevelAndPrice(g, this.x + 10, (yy - 5) - (kc + 5), isShopBuy, true, item);
                }
            }
        }
        paintFillLine(g, this.x, ((this.y + 40) + (vt.size() * kc)) - 5, 1);
        if (isOne) {
            if (this.select == 0 || this.select == 1) {
                Canvas.resetTrans(g);
                paintLevelAndPrice(g, this.x + 10, ((this.y + 32) - kc) + (this.select * kc), isShopBuy, true, (ItemInInventory) vt.elementAt(this.select));
            }
        }
    }

    public void paintShowTxt(Graphics g) {
        Canvas.resetTrans(g);
        g.setColor(game.pokemon.GiaoDien.VANG);
        g.fillRect(this.xShowTxt - 1, this.yShowTxt - 1, this.wShowTxt + 2, this.hShowTxt + 2);
        g.setColor(game.pokemon.GiaoDien.NAVY);
        g.fillRect(this.xShowTxt, this.yShowTxt, this.wShowTxt, this.hShowTxt);
        this.cmrShowTxt.setStyle(this.arrInfo.length, Screen.ITEM_HEIGHT + 2, this.xShowTxt, this.yShowTxt, this.wShowTxt, this.hShowTxt, true, 0);
        this.cmrShowTxt.setClip(g, this.xShowTxt, this.yShowTxt, this.wShowTxt, this.hShowTxt);
        int yy = 0;
        for (int i = 0; i < this.arrInfo.length; i++) {
            if (i == 1 && this.arrayKhamNgoc != null && this.arrayKhamNgoc.length > 0) {
                for (int j = 0; j < this.arrayKhamNgoc.length; j++) {
                    g.drawImage(Res.imgKham, this.xShowTxt + 12 + (j * 20), this.yShowTxt + 12 + yy, 3);
                    if (this.arrayKhamNgoc[j] != -1) {
                        Res.paintGem(g, this.arrayKhamNgoc[j], this.xShowTxt + 12 + (j * 20), this.yShowTxt + 12 + yy);
                    }
                }
                yy += 18;
            }
            if (!this.arrInfo[i].equals("")) {
                byte color = (byte) (this.arrInfo[i].charAt(0) - '0');
                byte indexsub = 1;
                if (!WindowInfoScr.isDegit(this.arrInfo[i].charAt(0))) {
                    color = 0;
                    indexsub = 0;
                }
                Font.normalFont[color >= 5 ? (byte) 0 : color].drawString(g, this.arrInfo[i].substring(indexsub), this.xShowTxt + 4, this.yShowTxt + 4 + (i * (Screen.ITEM_HEIGHT + 2)) + yy, 0);
            }
        }
    }

    @Override
    public void update() {
        if (this.lastScreen != null) {
            this.lastScreen.update();
        }
        if (this.countL > 0) {
            this.countL--;
        }
        if (this.countL <= 0) {
            this.countL = 5;
        }
        if (this.xRunL < 0) {
            this.xRunL++;
        }
        if (this.xRunR > 0) {
            this.xRunR--;
        }
        if (Canvas.currentDialog == null && !Canvas.menu.showMenu && !game.pokemon.ManCu.bat()) {
            if (this.isShowText) {
                ScrollResult r0 = this.cmrShowTxt.updateKey();
                if (r0.isDowning || r0.isFinish) {
                    this.selectTxt = r0.selected;
                }
                this.cmrShowTxt.updatecm();
            } else {
                ScrollResult r = this.scl.updateKey();
                if (r.isDowning || r.isFinish) {
                    this.select = r.selected;
                }
                if (Canvas.isPointer(this.x, this.y + 30, this.w0, this.h0 - 30) && Canvas.isPointerClick) {
                    Canvas.isPointerClick = false;
                    if (r.selected != -1 && this.center != null) {
                        this.center.action.perform();
                    }
                }
                this.scl.updatecm();
            }
        }
        super.update();
    }

    public void updatePoint() {
        switch (this.indexMainTile) {
            case 0:
                if (Canvas.isPointerClick) {
                    if (Canvas.isPointer((Canvas.w / 2) - 40 <= (this.x + 6) + wStr ? this.x + 6 + wStr : (Canvas.w / 2) - 40, this.y + 30, 100, 4 * (Screen.ITEM_HEIGHT + 10))) {
                        int aa = (Canvas.py - (this.y + 30)) / (Screen.ITEM_HEIGHT + 10);
                        Canvas.isPointerClick = false;
                        setCmdLeft();
                        if (aa >= 0 && aa <= 3) {
                            this.select = aa;
                            if (this.center != null) {
                                this.center.action.perform();
                            }
                        }
                    }
                }
                break;
        }
        if (Canvas.isPointerClick) {
            this.isFocusMainTab = false;
            Canvas.isPointerClick = false;
            if (Canvas.py >= this.y && Canvas.py <= this.y + 25) {
                if (Canvas.px > this.x && Canvas.px < Canvas.w / 2) {
                    this.indexMainTile--;
                    if (this.indexMainTile < 0) {
                        this.indexMainTile = tile.length - 1;
                    }
                    this.xRunL = -5;
                    this.scl.clear();
                    this.select = -1;
                    setCmdLeft();
                } else if (Canvas.px < (Canvas.w / 2) + (this.w0 / 2) && Canvas.px > Canvas.w / 2) {
                    this.indexMainTile++;
                    if (this.indexMainTile > tile.length - 1) {
                        this.indexMainTile = 0;
                    }
                    this.xRunR = 5;
                    this.scl.clear();
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
        if (game.pokemon.ManCu.bat() && !this.boc_updateKey && (Canvas.isPointerClick || Canvas.isPointerDown || Canvas.isPointerRelease)) {
            this.boc_updateKey = true;
            try {
                game.pokemon.ManCu.phimKhongCham(new Runnable() {
                    public void run() {
                        ShopOnline.this.updateKey();
                    }
                });
            } finally {
                this.boc_updateKey = false;
            }
            return;
        }
        if (this.isFocusMainTab) {
            if (Canvas.keyPressed[4]) {
                Canvas.keyPressed[4] = false;
                this.indexMainTile--;
                if (this.indexMainTile < 0) {
                    this.indexMainTile = tile.length - 1;
                }
                this.xRunL = -5;
                this.scl.clear();
                this.select = -1;
                setCmdLeft();
            } else if (Canvas.keyPressed[6]) {
                Canvas.keyPressed[6] = false;
                this.indexMainTile++;
                if (this.indexMainTile > tile.length - 1) {
                    this.indexMainTile = 0;
                }
                this.xRunR = 5;
                this.scl.clear();
                this.select = -1;
                setCmdLeft();
            }
            if (Canvas.keyPressed[8]) {
                Canvas.keyPressed[8] = false;
                this.select = 0;
                this.isFocusMainTab = false;
                setCmdLeft();
            }
        } else {
            Vector allItem = (Vector) ALL_ITEM.elementAt(this.indexMainTile);
            int size = allItem.size();
            if (this.isSelectTypeFind && this.indexMainTile == 1) {
                if (Canvas.keyPressed[2]) {
                    Canvas.keyPressed[2] = false;
                    this.select--;
                    if (this.select <= -1) {
                        this.select = -1;
                        this.isFocusMainTab = true;
                        setCmdLeft();
                    }
                    this.scl.moveTo(this.select * this.scl.ITEM_SIZE);
                } else if (Canvas.keyPressed[8]) {
                    Canvas.keyPressed[8] = false;
                    this.select++;
                    if (this.select > tileFind[this.typeSearch].length - 1) {
                        this.select = 0;
                    }
                    this.scl.moveTo(this.select * this.scl.ITEM_SIZE);
                }
            } else if (!this.isFocusMainTab) {
                if (Canvas.keyPressed[2]) {
                    Canvas.keyPressed[2] = false;
                    if (this.isShowText) {
                        this.selectTxt--;
                        if (this.selectTxt < 0) {
                            this.selectTxt = this.arrInfo.length - 1;
                        }
                        this.cmrShowTxt.moveTo(this.selectTxt * this.cmrShowTxt.ITEM_SIZE);
                    } else {
                        this.select--;
                        if (this.select <= -1) {
                            this.select = -1;
                            this.isFocusMainTab = true;
                        }
                        setCmdLeft();
                        this.scl.moveTo(this.select * this.scl.ITEM_SIZE);
                    }
                } else if (Canvas.keyPressed[8]) {
                    Canvas.keyPressed[8] = false;
                    if (this.isShowText) {
                        this.selectTxt++;
                        if (this.selectTxt > this.arrInfo.length - 1) {
                            this.selectTxt = 0;
                        }
                        this.cmrShowTxt.moveTo(this.selectTxt * this.cmrShowTxt.ITEM_SIZE);
                    } else {
                        this.select++;
                        if (this.select > size - 1) {
                            this.select = 0;
                        }
                        setCmdLeft();
                        this.scl.moveTo(this.select * this.scl.ITEM_SIZE);
                    }
                }
            }
        }
        super.updateKey();
        if (Canvas.currentDialog == null && !Canvas.menu.showMenu && !this.isShowText) {
            updatePoint();
        }
    }

    public void paintLevelAndPrice(Graphics g, int x, int y, boolean isShopBuy, boolean isBuy, ItemInInventory item) {
    }

    public void doFind() {
        this.isSelectTypeFind = false;
        this.select = -1;
        if (this.typeSearch == 0) {
            GameService.gI().doSearchItemSell(this.idLevel, ID_TYPE_ITEM[this.idType], this.idColor, this.idPham + 1, this.idPlus);
        } else if (this.typeSearch == 1) {
            GameService.gI().doSearchByName(this.nameChar);
        }
    }

    public void doselect() {
        if (Canvas.currentScreen == PageScr.gI()) {
            return;
        }
        if (this.indexMainTile == 1) {
            if (this.isSelectTypeFind) {
                Vector m = new Vector();
                if (this.typeSearch == 0) {
                    switch (this.select) {
                        case 0:
                            for (int i = 0; i < nameItem.length; i++) {
                                Command cmd = new Command(nameItem[i], new IAction() {
                                    @Override
                                    public void perform() {
                                        ShopOnline.this.idType = Canvas.menu.selected;
                                    }
                                });
                                m.addElement(cmd);
                            }
                            Canvas.menu.startAt(m, 2);
                            break;
                        case 1:
                            Canvas.inputDlg.setInfo("Nhập số", new IAction() {
                                @Override
                                public void perform() {
                                    try {
                                        String a = Canvas.inputDlg.tfInput.getText();
                                        ShopOnline.this.idLevel = Integer.parseInt(a);
                                        Canvas.endDlg();
                                        if (ShopOnline.this.idLevel < 0) {
                                            Canvas.startOKDlg("Nhập sai,vui lòng chỉ nhập số lớn hơn 0 ");
                                            ShopOnline.this.idLevel = 0;
                                        }
                                    } catch (Exception e) {
                                        Canvas.endDlg();
                                        Canvas.startOKDlg("Nhập sai,vui lòng chỉ nhập số");
                                    }
                                }
                            }, 3, 3, true);
                            Canvas.inputDlg.show();
                            break;
                        case 2:
                            for (int i2 = 0; i2 < Pham.length; i2++) {
                                Command cmd2 = new Command(Pham[i2], new IAction() {
                                    @Override
                                    public void perform() {
                                        ShopOnline.this.idPham = Canvas.menu.selected;
                                    }
                                });
                                m.addElement(cmd2);
                            }
                            Canvas.menu.startAt(m, 2);
                            break;
                        case 3:
                            for (int i3 = 0; i3 < Color.length; i3++) {
                                Command cmd3 = new Command(Color[i3], new IAction() {
                                    @Override
                                    public void perform() {
                                        ShopOnline.this.idColor = Canvas.menu.selected;
                                    }
                                });
                                m.addElement(cmd3);
                            }
                            Canvas.menu.startAt(m, 2);
                            break;
                        case 4:
                            for (int i4 = 0; i4 < 16; i4++) {
                                Command cmd4 = new Command(new StringBuffer("Cộng ").append(i4).toString(), new IAction() {
                                    @Override
                                    public void perform() {
                                        ShopOnline.this.idPlus = Canvas.menu.selected;
                                    }
                                });
                                m.addElement(cmd4);
                            }
                            Canvas.menu.startAt(m, 2);
                            break;
                    }
                    return;
                }
                if (this.typeSearch == 1 && this.select > -1) {
                    Canvas.inputDlg.setInfo("Nhập tên", new IAction() {
                        @Override
                        public void perform() {
                            try {
                                ShopOnline.this.nameChar = Canvas.inputDlg.tfInput.getText();
                                Canvas.endDlg();
                                if (ShopOnline.this.nameChar.equals("")) {
                                    Canvas.startOKDlg("Tên không hợp lệ");
                                }
                            } catch (Exception e) {
                            }
                        }
                    }, 0, 25, true);
                    Canvas.inputDlg.show();
                    return;
                }
                return;
            }
            if (Canvas.isSmartPhone) {
                Vector vt = new Vector();
                vt.addElement(this.cmdShowText);
                Canvas.menu.startAt(vt, 2);
                return;
            }
            doView();
            return;
        }
        if (Canvas.isSmartPhone) {
            Vector vt2 = new Vector();
            vt2.addElement(this.cmdShowText);
            Canvas.menu.startAt(vt2, 2);
            return;
        }
        doView();
    }

    public void setItemInventory() {
        ALL_ITEM.setElementAt(ItemInInventory.getAllItemCanSell(), 3);
    }

    public boolean canBid() {
        if (this.indexMainTile == 0) {
            if (((Vector) ALL_ITEM.elementAt(this.indexMainTile)).size() <= 0 || this.isFocusMainTab || this.select >= ((Vector) ALL_ITEM.elementAt(this.indexMainTile)).size() || this.select < 0) {
                return false;
            }
            ItemInInventory item = (ItemInInventory) ((Vector) ALL_ITEM.elementAt(this.indexMainTile)).elementAt(this.select);
            if (item.prizeBet > 0) {
                return true;
            }
            return false;
        }
        if (this.indexMainTile != 1 || this.isSelectTypeFind || ((Vector) ALL_ITEM.elementAt(this.indexMainTile)).size() <= 0 || this.isFocusMainTab || this.select >= ((Vector) ALL_ITEM.elementAt(this.indexMainTile)).size() || this.select < 0) {
            return false;
        }
        ItemInInventory item2 = (ItemInInventory) ((Vector) ALL_ITEM.elementAt(this.indexMainTile)).elementAt(this.select);
        if (item2.prizeBet > 0) {
            return true;
        }
        return false;
    }

    public void setCmdLeft() {
        if (this.indexMainTile == 3) {
            setItemInventory();
        }
        if (this.indexMainTile == 0) {
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
                                if (ShopOnline.this.select >= 0) {
                                    ItemInInventory item = (ItemInInventory) ((Vector) ShopOnline.ALL_ITEM.elementAt(ShopOnline.this.indexMainTile)).elementAt(ShopOnline.this.select);
                                    ShopOnline.this.doAcceptBuy(0, item);
                                }
                            }
                        });
                        return;
                    }
                    return;
                }
            }
            this.left = new Command("Menu", new AnonymousClass21());
            return;
        }
        if (this.indexMainTile == 1) {
            this.left = new Command("Menu", new AnonymousClass26());
            return;
        }
        if (this.indexMainTile == 2) {
            this.left = this.cmdDoNothing;
            return;
        }
        if (this.indexMainTile == 3) {
            this.left = this.cmdSell;
            return;
        }
        if (this.indexMainTile == 4) {
            Vector v = (Vector) ALL_ITEM.elementAt(this.indexMainTile);
            if (v.size() > 0 || money > 0) {
                this.left = new Command("Menu", new AnonymousClass38(v));
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
        if (this.indexMainTile == 5) {
            if (((Vector) ALL_ITEM.elementAt(this.indexMainTile)).size() == 0 || this.isFocusMainTab) {
                this.left = new Command("", new IAction() {
                    @Override
                    public void perform() {
                    }
                });
            } else {
                this.left = new Command("Menu", new AnonymousClass43());
            }
        }
    }

    class AnonymousClass21 implements IAction {
        AnonymousClass21() {
        }

        @Override
        public void perform() {
            ShopOnline.this.isShowText = false;
            Vector menu = new Vector();
            menu.addElement(new Command("Mua", new IAction() {
                @Override
                public void perform() {
                    if (ShopOnline.this.select >= 0) {
                        ShopOnline.this.doAcceptBuy(0, (ItemInInventory) ((Vector) ShopOnline.ALL_ITEM.elementAt(ShopOnline.this.indexMainTile)).elementAt(ShopOnline.this.select));
                    }
                }
            }));
            menu.addElement(new Command("Đấu giá", new AnonymousClass23(this)));
            Canvas.menu.startAt(menu, 0);
        }
    }

    class AnonymousClass23 implements IAction {
        final AnonymousClass21 this$1;

        AnonymousClass23(AnonymousClass21 anonymousClass21) {
            this.this$1 = anonymousClass21;
        }

        @Override
        public void perform() {
            if (ShopOnline.this.select >= 0) {
                ItemInInventory item = (ItemInInventory) ((Vector) ShopOnline.ALL_ITEM.elementAt(ShopOnline.this.indexMainTile)).elementAt(ShopOnline.this.select);
                Canvas.inputDlg.setInfo("Nhập giá", new AnonymousClass24(this, item), 3, 10, true);
                Canvas.inputDlg.show();
            }
        }
    }

    class AnonymousClass24 implements IAction {
        final AnonymousClass23 this$2;
        private final ItemInInventory val$item;

        AnonymousClass24(AnonymousClass23 anonymousClass23, ItemInInventory itemInInventory) {
            this.this$2 = anonymousClass23;
            this.val$item = itemInInventory;
        }

        @Override
        public void perform() {
            try {
                String a = Canvas.inputDlg.tfInput.getText();
                final int price = Integer.parseInt(a);
                Canvas.endDlg();
                if (price < 0 || price <= this.val$item.prizeBet || price >= this.val$item.prize) {
                    Canvas.startOKDlg(new StringBuffer("Giá đặt phải lớn hơn ").append(this.val$item.prizeBet).append(" và nhỏ hơn ").append(this.val$item.prize).toString());
                } else {
                    Canvas.startYesNoDlg(new StringBuffer("Bạn có muốn đặt giá cho ").append(this.val$item.getTemplate().name).append(" với giá ").append(price).append(" không?").toString(), new IAction() {
                        @Override
                        public void perform() {
                            if (ShopOnline.this.select >= 0) {
                                GameService.gI().doBid(ShopOnline.this.select, 0, price);
                                Canvas.endDlg();
                            }
                        }
                    });
                }
            } catch (Exception e) {
                Canvas.endDlg();
                Canvas.startOKDlg("Nhập sai,vui lòng chỉ nhập số");
            }
        }
    }

    class AnonymousClass26 implements IAction {
        AnonymousClass26() {
        }

        @Override
        public void perform() {
            ShopOnline.this.isShowText = false;
            Vector menu = new Vector();
            if (!ShopOnline.this.isFocusMainTab && ((Vector) ShopOnline.ALL_ITEM.elementAt(ShopOnline.this.indexMainTile)).size() > 0 && ShopOnline.this.select > -1) {
                if (ShopOnline.this.select >= 0) {
                    ItemInInventory item = (ItemInInventory) ((Vector) ShopOnline.ALL_ITEM.elementAt(ShopOnline.this.indexMainTile)).elementAt(ShopOnline.this.select);
                    if (!item.isMyItem()) {
                        menu.addElement(new Command("Mua", new AnonymousClass27(this, item)));
                        if (ShopOnline.this.canBid()) {
                            menu.addElement(new Command("Đấu giá", new AnonymousClass29(this, item)));
                        }
                    }
                } else {
                    return;
                }
            }
            if (ShopOnline.this.isSelectTypeFind) {
                menu.addElement(new Command("Tìm", new IAction() {
                    @Override
                    public void perform() {
                        ShopOnline.this.doFind();
                    }
                }));
            }
            if (ShopOnline.this.typeSearch != 0) {
                menu.addElement(new Command("Tìm theo loại", new IAction() {
                    @Override
                    public void perform() {
                        ShopOnline.this.reset();
                        ShopOnline.this.typeSearch = (byte) 0;
                    }
                }));
            }
            if (ShopOnline.this.typeSearch != 1) {
                menu.addElement(new Command("Tìm theo người bán", new IAction() {
                    @Override
                    public void perform() {
                        ShopOnline.this.reset();
                        ShopOnline.this.typeSearch = (byte) 1;
                    }
                }));
            }
            if (((Vector) ShopOnline.ALL_ITEM.elementAt(ShopOnline.this.indexMainTile)).size() > 0) {
                menu.addElement(new Command("Kết quả", new IAction() {
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
                    menu.addElement(new Command("Trang sau", new IAction() {
                        @Override
                        public void perform() {
                            short[] sArr = ShopOnline.indexPage;
                            int i = ShopOnline.this.indexMainTile;
                            sArr[i] = (short) (sArr[i] + 1);
                        }
                    }));
                }
                if (ShopOnline.indexPage[ShopOnline.this.indexMainTile] > 0) {
                    menu.addElement(new Command("Trang trước", new IAction() {
                        @Override
                        public void perform() {
                            short[] sArr = ShopOnline.indexPage;
                            int i = ShopOnline.this.indexMainTile;
                            sArr[i] = (short) (sArr[i] - 1);
                        }
                    }));
                }
            }
            Canvas.menu.startAt(menu, 0);
        }
    }

    class AnonymousClass27 implements IAction {
        final AnonymousClass26 this$1;
        private final ItemInInventory val$item;

        AnonymousClass27(AnonymousClass26 anonymousClass26, ItemInInventory itemInInventory) {
            this.this$1 = anonymousClass26;
            this.val$item = itemInInventory;
        }

        @Override
        public void perform() {
            Canvas.startYesNoDlg(new StringBuffer("Bạn có muốn mua ").append(this.val$item.getTemplate().name).append(" với giá ").append(this.val$item.prize).append(" không?").toString(), new IAction() {
                @Override
                public void perform() {
                    if (ShopOnline.this.select >= 0) {
                        ShopOnline.this.doAcceptBuy(1, (ItemInInventory) ((Vector) ShopOnline.ALL_ITEM.elementAt(ShopOnline.this.indexMainTile)).elementAt(ShopOnline.this.select));
                    }
                }
            });
        }
    }

    class AnonymousClass29 implements IAction {
        final AnonymousClass26 this$1;
        private final ItemInInventory val$item;

        AnonymousClass29(AnonymousClass26 anonymousClass26, ItemInInventory itemInInventory) {
            this.this$1 = anonymousClass26;
            this.val$item = itemInInventory;
        }

        @Override
        public void perform() {
            Canvas.inputDlg.setInfo("Nhập giá", new AnonymousClass30(this, this.val$item), 3, 10, true);
            Canvas.inputDlg.show();
        }
    }

    class AnonymousClass30 implements IAction {
        final AnonymousClass29 this$2;
        private final ItemInInventory val$item;

        AnonymousClass30(AnonymousClass29 anonymousClass29, ItemInInventory itemInInventory) {
            this.this$2 = anonymousClass29;
            this.val$item = itemInInventory;
        }

        @Override
        public void perform() {
            try {
                String a = Canvas.inputDlg.tfInput.getText();
                final int price = Integer.parseInt(a);
                Canvas.endDlg();
                if (price < 0 || price <= this.val$item.prizeBet || price >= this.val$item.prize) {
                    Canvas.startOKDlg(new StringBuffer("Giá đặt phải lớn hơn ").append(this.val$item.prizeBet).append(" và nhỏ hơn ").append(this.val$item.prize).toString());
                } else {
                    Canvas.startYesNoDlg(new StringBuffer("Bạn có muốn đặt giá cho ").append(this.val$item.getTemplate().name).append(" với giá ").append(price).append(" không?").toString(), new IAction() {
                        @Override
                        public void perform() {
                            if (ShopOnline.this.select >= 0) {
                                GameService.gI().doBid(ShopOnline.this.select, 1, price);
                                Canvas.endDlg();
                            }
                        }
                    });
                }
            } catch (Exception e) {
                Canvas.endDlg();
                Canvas.startOKDlg("Nhập sai,vui lòng chỉ nhập số");
            }
        }
    }

    class AnonymousClass38 implements IAction {
        private final Vector val$v;

        AnonymousClass38(Vector vector) {
            this.val$v = vector;
        }

        @Override
        public void perform() {
            ShopOnline.this.isShowText = false;
            Vector menu = new Vector();
            if (ShopOnline.money > 0) {
                menu.addElement(new Command("Rút tiền", new IAction() {
                    @Override
                    public void perform() {
                        GameService.gI().doGetMoneySellInventory();
                    }
                }));
            }
            if (this.val$v.size() > 0 && ShopOnline.this.select >= 0) {
                menu.addElement(new Command("Bỏ vào hành trang", new IAction() {
                    @Override
                    public void perform() {
                        GameService.gI().doGetItemBackInventory(ShopOnline.this.select);
                    }
                }));
            }
            Canvas.menu.startAt(menu, 2);
        }
    }

    class AnonymousClass43 implements IAction {
        AnonymousClass43() {
        }

        @Override
        public void perform() {
            ShopOnline.this.isShowText = false;
            Vector menu = new Vector();
            menu.addElement(new Command("Mua", new IAction() {
                @Override
                public void perform() {
                    ShopOnline.this.doAcceptBuy(2, (ItemInInventory) ((Vector) ShopOnline.ALL_ITEM.elementAt(ShopOnline.this.indexMainTile)).elementAt(ShopOnline.this.select));
                }
            }));
            menu.addElement(new Command("Đặt giá", new AnonymousClass45(this)));
            Canvas.menu.startAt(menu, 0);
        }
    }

    class AnonymousClass45 implements IAction {
        final AnonymousClass43 this$1;

        AnonymousClass45(AnonymousClass43 anonymousClass43) {
            this.this$1 = anonymousClass43;
        }

        @Override
        public void perform() {
            ItemInInventory item = (ItemInInventory) ((Vector) ShopOnline.ALL_ITEM.elementAt(ShopOnline.this.indexMainTile)).elementAt(ShopOnline.this.select);
            Canvas.inputDlg.setInfo("Nhập giá", new AnonymousClass46(this, item), 3, 10, true);
            Canvas.inputDlg.show();
        }
    }

    class AnonymousClass46 implements IAction {
        final AnonymousClass45 this$2;
        private final ItemInInventory val$item;

        AnonymousClass46(AnonymousClass45 anonymousClass45, ItemInInventory itemInInventory) {
            this.this$2 = anonymousClass45;
            this.val$item = itemInInventory;
        }

        @Override
        public void perform() {
            try {
                String a = Canvas.inputDlg.tfInput.getText();
                final int price = Integer.parseInt(a);
                Canvas.endDlg();
                if (price < 0 || price <= this.val$item.prizeBet || price >= this.val$item.prize) {
                    Canvas.startOKDlg(new StringBuffer("Giá đặt phải lớn hơn ").append(this.val$item.prizeBet).append(" và nhỏ hơn ").append(this.val$item.prize).toString());
                } else {
                    Canvas.startYesNoDlg(new StringBuffer("Bạn có muốn đặt giá cho ").append(this.val$item.getTemplate().name).append(" với giá ").append(price).append(" không?").toString(), new IAction() {
                        @Override
                        public void perform() {
                            GameService.gI().doBid(ShopOnline.this.select, 2, price);
                            Canvas.endDlg();
                        }
                    });
                }
            } catch (Exception e) {
                Canvas.endDlg();
                Canvas.startOKDlg("Nhập sai,vui lòng chỉ nhập số");
            }
        }
    }

    /** 31/10/2026: máy có con trỏ - nhập giá bán / giá đấu giá bằng ô nhập chung (hai bước) thay khung PageScr cũ. */
    static void hoiGiaBan(final ItemInInventory item) {
        Canvas.inputDlg.setInfo("Giá bán (xu)", new IAction() {
            public void perform() {
                final int gia;
                try {
                    gia = Integer.parseInt(Canvas.inputDlg.tfInput.getText().trim());
                } catch (Exception e) {
                    Canvas.endDlg();
                    Canvas.startOKDlg("Giá bán không hợp lệ");
                    return;
                }
                Canvas.endDlg();
                Canvas.inputDlg.setInfo("Giá khởi điểm đấu giá (xu, nhỏ hơn giá bán)", new IAction() {
                    public void perform() {
                        final int dat;
                        try {
                            dat = Integer.parseInt(Canvas.inputDlg.tfInput.getText().trim());
                        } catch (Exception e) {
                            Canvas.endDlg();
                            Canvas.startOKDlg("Giá đặt không hợp lệ");
                            return;
                        }
                        Canvas.endDlg();
                        if (gia < dat) {
                            Canvas.startOKDlg("Giá đặt phải nhỏ hơn giá bán");
                            return;
                        }
                        Canvas.startYesNoDlg("Đặt bán vật phẩm này với giá " + game.pokemon.TienTe.gon(gia) + " xu?", new IAction() {
                            public void perform() {
                                GameService.gI().doRequestSelItem(0, item.itemID, gia, dat);
                                Canvas.startOKDlg("Đã gửi thông tin bán");
                            }
                        });
                    }
                }, 3, 9, true);
                Canvas.inputDlg.tfInput.setText("10000");
                Canvas.inputDlg.show();
            }
        }, 3, 9, true);
        Canvas.inputDlg.tfInput.setText("100000");
        Canvas.inputDlg.show();
    }

    public static void popupBuyItem(final ItemInInventory item) {
        if (game.pokemon.MenuNguCanh.bat()) {
            hoiGiaBan(item);
            return;
        }
        final TField tfSeri = new TField(10, 40, 120, Screen.deltaY);
        tfSeri.setIputType(3);
        tfSeri.isFocus = true;
        tfSeri.setText("100000");
        PageScr.gI().right = tfSeri.cmdClear;
        final TField tfMaso = new TField(10, 75, 120, Screen.deltaY);
        tfMaso.setIputType(3);
        tfMaso.setText("10000");
        PageScr.gI().setInfo(Canvas.hw - 70, (Canvas.h - Screen.deltaY) - 120, 140, 100, "NHẬP GIÁ BÁN", new Command("Bán", new IAction() {
            @Override
            public void perform() {
                try {
                    final int price = Integer.parseInt(tfSeri.getText().trim());
                    final int priceBid = Integer.parseInt(tfMaso.getText().trim());
                    if (price < priceBid) {
                        Canvas.startOKDlg("Giá đặt phải nhỏ hơn giá bán");
                        return;
                    }
                    String string = new StringBuffer("Bạn có muốn đặt bán vật phẩm này với giá ").append(tfSeri.getText().trim()).append("  không?").toString();
                    final ItemInInventory itemInInventory = item;
                    Canvas.startYesNoDlg(string, new IAction() {
                        @Override
                        public void perform() {
                            GameService.gI().doRequestSelItem(0, itemInInventory.itemID, price, priceBid);
                            PageScr.gI().left.action.perform();
                            Canvas.startOKDlg("Đã gửi thông tin bán ");
                        }
                    });
                } catch (Exception e) {
                    Canvas.startOKDlg("Giá bán không hợp lệ");
                }
            }
        }));
        PageScr.gI().layer = new Layer() {
            @Override
            public void update() {
                if (Canvas.isKeyPressed(2) || Canvas.isKeyPressed(8)) {
                    if (tfSeri.isFocus) {
                        tfSeri.isFocus = false;
                        tfMaso.isFocus = true;
                        PageScr.gI().right = tfMaso.cmdClear;
                    } else {
                        tfSeri.isFocus = true;
                        tfMaso.isFocus = false;
                        PageScr.gI().right = tfSeri.cmdClear;
                    }
                }
                tfSeri.update();
                tfMaso.update();
            }

            @Override
            public void keyPress(int keyCode) {
                tfSeri.keyPressed(keyCode);
                tfMaso.keyPressed(keyCode);
            }

            @Override
            public void paint(Graphics g, int x, int y) {
                Font.borderFont.drawString(g, "Giá bán", 10, 26, 0);
                Font.borderFont.drawString(g, "Giá đặt", 10, 61, 0);
                tfSeri.paint(g);
                g.setClip(0, 0, 140, 100);
                tfMaso.paint(g);
            }
        };
        PageScr.gI().switchToMe(Canvas.shop);
    }

    public void onItemInfo() {
        if (this.isShowText) {
            doView();
        }
    }
}
