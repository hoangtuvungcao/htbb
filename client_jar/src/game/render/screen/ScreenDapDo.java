package game.render.screen;

import game.model.Char;
import game.model.Command;
import game.model.Cout;
import game.model.GemTemp;
import game.model.GemTemplate;
import game.model.IAction;
import game.model.ItemInInventory;
import game.model.MainChar;
import game.model.Scroll;
import game.model.ScrollResult;
import game.networklogic.GameService;
import game.render.Canvas;
import game.render.Res;
import java.util.Vector;
import javax.microedition.lcdui.Graphics;
import network.Message;

public class ScreenDapDo extends Screen {
    public static ScreenDapDo me;
    public static int wCell;
    public static int hCell;
    public static int xDr;
    public static int yDr;
    public int index;
    public int xSh;
    public int ySh;
    public int wSh;
    public int hSh;
    public int selectItemInventory;
    public int lastIndex1;
    public int h0;
    public int indexTabNguyenLieu;
    public int lastIndex2;
    public Screen last;
    public boolean isShowText;
    public static final byte TYPE2 = 0;
    public static final byte TYPE4 = 1;
    public static final byte TYPE_ANIMAL = 2;
    public static final byte TYPE_LOCK_ITEM = 3;
    public static final byte TYPE_THEM_DONG = 4;
    public byte type;
    Command cmdClose;
    Command cmdErr;
    Command cmdView;
    Command cmdEp;
    Command cmdContinue;
    int cout;
    static ItemInInventory itemSelect;
    int[] xItem;
    int[] yItem;
    int[] xTo;
    int[] yTo;
    boolean isEp;
    boolean isShowPopup;
    int xPopup;
    int yPopup;
    public byte currentRow;
    public byte timeDelay;
    int selectItem;
    public static boolean LKDLock;
    public static boolean MMLock;
    public static boolean BHLock;
    public static short itemID;
    public static int hImg = 80;
    public static int wImg = 40;
    public static Vector vnguyenLieu = new Vector();
    static GemTemplate selectGem = null;
    static GemTemplate selectGemBH = null;
    static GemTemplate selectGemMM = null;
    public static Vector listItem = new Vector();
    public static boolean isFinishImbue = true;
    public static short idLkd = -1;
    public static short idMM = -1;
    public static short idBH = -1;
    public static byte COLOR_WHITE = 0;
    public static byte COLOR_GREEN = 1;
    public static byte COLOR_RED = 2;
    public static byte COLOR_BLUE = 3;
    public static byte COLOR_MAGENTA = 4;
    public static byte COLOR_YELLOW = 5;
    public Scroll mScrollItemInventory = new Scroll();
    public Scroll mScrollShowText = new Scroll();
    public Scroll mScrollNguyenLieu = new Scroll();
    public int wTab = 28;
    public String[] arrShowText = {""};
    public String tile = "Cộng thuộc tính";
    String infoPoup = "Thong bao";

    public static ScreenDapDo gI() {
        if (me != null) {
            return me;
        }
        ScreenDapDo screenDapDo = new ScreenDapDo();
        me = screenDapDo;
        return screenDapDo;
    }

    public void setData(Message msg) {
    }

    public void resetData() {
        this.mScrollItemInventory.clear();
        this.mScrollNguyenLieu.clear();
        this.mScrollShowText.clear();
        vnguyenLieu = new Vector();
        this.index = Canvas.isSmartPhone ? -1 : 0;
        this.selectItemInventory = 0;
        this.indexTabNguyenLieu = 0;
        this.isShowPopup = false;
        setSizeCell(this.type, false);
        this.currentRow = (byte) 0;
        this.lastIndex1 = 0;
        this.lastIndex2 = 0;
        if (Canvas.isSmartPhone) {
            this.currentRow = (byte) -1;
            this.lastIndex1 = -1;
            this.lastIndex2 = -1;
        }
        this.lastIndex2 = 0;
        if (Canvas.isSmartPhone) {
            this.lastIndex2 = -1;
        }
    }

    public void setSizeCell(byte type, boolean isContinue) {
        int xbg = (xDr + (wCell / 2)) - 44;
        int ybg = (yDr + (hCell / 2)) - 35;
        if (Canvas.isSmartPhone) {
            xbg = (xDr + (wCell / 4)) - 44;
        }
        if (type == 0) {
            this.xItem = new int[2];
            this.xTo = new int[2];
            this.yItem = new int[2];
            this.xItem[0] = xbg;
            this.xItem[1] = xbg + 44 + 22;
            this.yItem[0] = ybg;
            this.yItem[1] = ybg;
            this.xTo[0] = this.xItem[0];
            this.xTo[1] = this.xItem[1];
        } else if (type == 1) {
            this.cmdEp.caption = "Đập";
            this.xItem = new int[4];
            this.xTo = new int[4];
            this.yItem = new int[4];
            this.xItem[1] = xbg - 22;
            this.xItem[2] = (xbg + 44) - 11;
            this.xItem[3] = xbg + 44 + 22 + 22;
            this.xItem[0] = (xbg + 44) - 11;
            this.yItem[1] = ybg + 11;
            this.yItem[2] = ybg + 11;
            this.yItem[3] = ybg + 11;
            this.yItem[0] = ybg - 18;
            this.xTo[0] = this.xItem[0];
            this.xTo[1] = this.xItem[1];
            this.xTo[2] = this.xItem[2];
            this.xTo[3] = this.xItem[3];
            this.xPopup = (xbg + 44) - 1;
            this.yPopup = ybg + 40;
        } else if (type == 4) {
            this.cmdEp.caption = "Đập";
            this.xItem = new int[3];
            this.xTo = new int[3];
            this.yItem = new int[3];
            this.xItem[1] = xbg + 44 + 20;
            this.xItem[0] = (xbg + 44) - 11;
            this.xItem[2] = (xbg + 44) - 42;
            this.yItem[1] = ybg + 22;
            this.yItem[0] = ybg - 11;
            this.yItem[2] = ybg + 22;
            this.xTo[0] = this.xItem[0];
            this.xTo[1] = this.xItem[1];
            this.xTo[2] = this.xItem[1];
            this.xPopup = (xbg + 44) - 1;
            this.yPopup = ybg + 40;
        } else {
            this.cmdEp.caption = "Khóa";
            this.xItem = new int[2];
            this.xTo = new int[2];
            this.yItem = new int[2];
            this.xItem[1] = (xbg + 44) - 11;
            this.xItem[0] = (xbg + 44) - 11;
            this.yItem[1] = ybg + 11;
            this.yItem[0] = ybg - 18;
            this.xTo[0] = this.xItem[0];
            this.xTo[1] = this.xItem[1];
            this.xPopup = (xbg + 44) - 1;
            this.yPopup = ybg + 40;
        }
        Cout.println(new StringBuffer("TYPE SCREENDAPDO ").append((int) type).toString());
        this.cout = 0;
        if (isContinue) {
            this.currentRow = (byte) 0;
            if (Canvas.isSmartPhone) {
                this.currentRow = (byte) -1;
            }
            this.left = this.cmdEp;
            this.isShowText = false;
            this.mScrollShowText.clear();
            this.isEp = false;
        } else {
            this.isEp = false;
            this.left = this.cmdEp;
        }
        this.type = type;
        resetSelect();
        getListGem(true, false);
    }

    public ScreenDapDo() {
        this.index = -1;
        wCell = Canvas.w - 10;
        hCell = (Canvas.h - Screen.ITEM_HEIGHT) - 10;
        if (wCell > 176) {
            wCell = 176;
        }
        if (hCell > 220) {
            hCell = 220;
        }
        if (Canvas.w >= 320 && Canvas.isSmartPhone) {
            wCell = 300;
        }
        xDr = (Canvas.w / 2) - (wCell / 2);
        yDr = ((Canvas.h - Screen.ITEM_HEIGHT) / 2) - (hCell / 2);
        if (yDr < 5) {
            yDr = 5;
        }
        if (xDr < 5) {
            xDr = 5;
        }
        this.wSh = wCell;
        if (Canvas.isSmartPhone && wCell == 300) {
            this.xSh = xDr + (wCell / 2);
            this.ySh = yDr + 55;
            this.wSh = (wCell / 2) - 8;
            this.hSh = hCell - 54;
        }
        this.h0 = hCell - 54;
        this.cmdClose = new Command("Đóng", new IAction() {
            @Override
            public void perform() {
                if (ScreenDapDo.this.isShowText) {
                    ScreenDapDo.this.isShowText = false;
                    ScreenDapDo.this.center = ScreenDapDo.this.cmdView;
                    ScreenDapDo.this.mScrollShowText.clear();
                    ScreenDapDo.this.arrShowText = new String[]{""};
                    if (ScreenDapDo.this.isEp) {
                        ScreenDapDo.this.left = ScreenDapDo.this.cmdContinue;
                        return;
                    } else {
                        ScreenDapDo.this.left = ScreenDapDo.this.cmdEp;
                        return;
                    }
                }
                ScreenDapDo.this.resetData();
                ScreenDapDo.this.last.switchToMe();
            }
        });
        this.cmdView = new Command(Canvas.isSmartPhone ? "" : "", new AnonymousClass2());
        this.cmdEp = new Command("Đập", new AnonymousClass7());
        this.cmdContinue = new Command("Tiếp tục", new IAction() {
            @Override
            public void perform() {
                ScreenDapDo.this.setSizeCell(ScreenDapDo.this.type, true);
            }
        });
        this.cmdErr = new Command("", new IAction() {
            @Override
            public void perform() {
            }
        });
        this.right = this.cmdClose;
        this.center = this.cmdView;
        if (!Canvas.isSmartPhone) {
            this.index = 0;
        }
        setData(null);
        this.lastIndex1 = 0;
        if (Canvas.isSmartPhone) {
            this.lastIndex1 = -1;
            this.currentRow = (byte) -1;
            this.lastIndex2 = -1;
        }
        this.lastIndex2 = 0;
        if (Canvas.isSmartPhone) {
            this.lastIndex2 = -1;
        }
    }

    class AnonymousClass2 implements IAction {
        AnonymousClass2() {
        }

        @Override
        public void perform() {
            Vector menu = new Vector();
            menu.addElement(new Command("Bỏ vào", new AnonymousClass3(this)));
            menu.addElement(new Command("Thông tin", new IAction() {
                @Override
                public void perform() {
                    if (Canvas.isSmartPhone) {
                        ScreenDapDo.this.timeDelay = (byte) 5;
                    }
                    ScreenDapDo.this.doView();
                }
            }));
            Canvas.menu.startAt(menu, 3);
            Canvas.currentDialog = null;
        }
    }

    class AnonymousClass3 implements IAction {
        final AnonymousClass2 this$1;

        AnonymousClass3(AnonymousClass2 anonymousClass2) {
            this.this$1 = anonymousClass2;
        }

        @Override
        public void perform() {
            if (Canvas.isSmartPhone) {
                ScreenDapDo.this.timeDelay = (byte) 5;
            }
            if (ScreenDapDo.this.currentRow == 1) {
                if (ScreenDapDo.this.selectItemInventory != -1 && ScreenDapDo.this.selectItemInventory <= ScreenDapDo.listItem.size() - 1) {
                    if (ScreenDapDo.this.type != 1 || ScreenDapDo.isFinishImbue) {
                        ScreenDapDo.itemSelect = (ItemInInventory) ScreenDapDo.listItem.elementAt(ScreenDapDo.this.selectItemInventory);
                        return;
                    } else {
                        Canvas.startOKDlg("Không thể chọn khi đang trong quá trình luyện tự động.", new IAction() {
                            @Override
                            public void perform() {
                                ScreenDapDo.this.right.action.perform();
                                Canvas.endDlg();
                            }
                        });
                        return;
                    }
                }
                return;
            }
            if (ScreenDapDo.this.currentRow == 0 && ScreenDapDo.vnguyenLieu.size() > 0) {
                if (ScreenDapDo.this.type == 0 || ScreenDapDo.this.type == 2 || ScreenDapDo.this.type == 3) {
                    ScreenDapDo.selectGem = (GemTemplate) ScreenDapDo.vnguyenLieu.elementAt(ScreenDapDo.this.indexTabNguyenLieu);
                    return;
                }
                if (ScreenDapDo.this.type == 4) {
                    GemTemplate gem = (GemTemplate) ScreenDapDo.vnguyenLieu.elementAt(ScreenDapDo.this.indexTabNguyenLieu);
                    GemTemp g = Res.getGemByID(gem.id);
                    if (!ScreenDapDo.isBot(g.type)) {
                        if (ScreenDapDo.this.isDaTienCap(g.type)) {
                            ScreenDapDo.selectGemBH = gem;
                            return;
                        }
                        return;
                    }
                    ScreenDapDo.selectGem = gem;
                    return;
                }
                if (ScreenDapDo.isFinishImbue) {
                    GemTemplate gem2 = (GemTemplate) ScreenDapDo.vnguyenLieu.elementAt(ScreenDapDo.this.indexTabNguyenLieu);
                    if (ScreenDapDo.isDaMayMan(gem2.id)) {
                        ScreenDapDo.selectGemMM = gem2;
                        return;
                    } else if (ScreenDapDo.isBH(gem2.id)) {
                        ScreenDapDo.selectGemBH = gem2;
                        return;
                    } else {
                        if (ScreenDapDo.isLKD(gem2.id)) {
                            ScreenDapDo.selectGem = gem2;
                            return;
                        }
                        return;
                    }
                }
                Canvas.startOKDlg("Không thể chọn khi đang trong quá trình luyện tự động.", new IAction() {
                    @Override
                    public void perform() {
                        ScreenDapDo.this.right.action.perform();
                        Canvas.endDlg();
                    }
                });
            }
        }
    }

    class AnonymousClass7 implements IAction {
        AnonymousClass7() {
        }

        @Override
        public void perform() {
            ScreenDapDo.this.currentRow = (byte) -1;
            if (ScreenDapDo.vnguyenLieu.size() <= 0) {
                Canvas.startOKDlg("Không có nguyên liệu.", new IAction() {
                    @Override
                    public void perform() {
                        ScreenDapDo.this.right.action.perform();
                        Canvas.endDlg();
                    }
                });
                return;
            }
            if (ScreenDapDo.itemSelect != null) {
                if (ScreenDapDo.this.type == 0 || ScreenDapDo.this.type == 2 || ScreenDapDo.this.type == 3 || ScreenDapDo.this.type == 4) {
                    if (ScreenDapDo.selectGem != null) {
                        if (ScreenDapDo.this.type == 0) {
                            GameService.gI().doRequestTachNguyenLIeu(ScreenDapDo.itemSelect.itemID, 1, ScreenDapDo.selectGem.id, ScreenDapDo.selectGem.lock ? 1 : 0);
                            ScreenDapDo.this.left = ScreenDapDo.this.cmdContinue;
                            ScreenDapDo.this.doEp();
                            return;
                        }
                        if (ScreenDapDo.this.type == 2) {
                            GameService.gI().doRequestTachNguyenLIeu(ScreenDapDo.itemSelect.itemID, 2, ScreenDapDo.selectGem.id, ScreenDapDo.selectGem.lock ? 1 : 0);
                            ScreenDapDo.this.left = ScreenDapDo.this.cmdContinue;
                            Canvas.startOKDlg("Xin chờ");
                            return;
                        } else {
                            if (ScreenDapDo.this.type == 4) {
                                if (ScreenDapDo.selectGemBH != null) {
                                    GameService.gI().doRequestThemThuocTinh(ScreenDapDo.itemSelect.itemID, 5, ScreenDapDo.selectGem.id, ScreenDapDo.selectGem.lock ? 1 : 0, ScreenDapDo.selectGemBH.id, ScreenDapDo.selectGemBH.lock ? 1 : 0);
                                    ScreenDapDo.this.left = ScreenDapDo.this.cmdContinue;
                                    Canvas.startOKDlg("Xin chờ");
                                    return;
                                }
                                Canvas.startOKDlg("Chưa đặt đá.", new IAction() {
                                    @Override
                                    public void perform() {
                                        ScreenDapDo.this.right.action.perform();
                                        Canvas.endDlg();
                                    }
                                });
                                return;
                            }
                            GameService.gI().doRequestTachNguyenLIeu(ScreenDapDo.itemSelect.itemID, 3, ScreenDapDo.selectGem.id, ScreenDapDo.selectGem.lock ? 1 : 0);
                            ScreenDapDo.this.left = ScreenDapDo.this.cmdContinue;
                            Canvas.startOKDlg("Xin chờ");
                            return;
                        }
                    }
                    Canvas.startOKDlg(ScreenDapDo.this.type == 4 ? "Bạn chưa chọn bột" : "Bạn chưa chọn đá thuộc tính.", new IAction() {
                        @Override
                        public void perform() {
                            ScreenDapDo.this.right.action.perform();
                            Canvas.endDlg();
                        }
                    });
                    return;
                }
                if (ScreenDapDo.selectGem == null) {
                    Canvas.startOKDlg("Bạn chưa chọn luyện kim dược", new IAction() {
                        @Override
                        public void perform() {
                            ScreenDapDo.this.right.action.perform();
                            Canvas.endDlg();
                        }
                    });
                    return;
                }
                final Vector idgem = new Vector();
                final Vector lock = new Vector();
                idgem.addElement(new StringBuffer(String.valueOf((int) ScreenDapDo.selectGem.id)).toString());
                lock.addElement(new StringBuffer(String.valueOf(ScreenDapDo.selectGem.lock ? 1 : 0)).toString());
                ScreenDapDo.LKDLock = ScreenDapDo.selectGem.lock;
                ScreenDapDo.idLkd = ScreenDapDo.selectGem.id;
                if (ScreenDapDo.selectGemBH != null) {
                    idgem.addElement(new StringBuffer(String.valueOf((int) ScreenDapDo.selectGemBH.id)).toString());
                    lock.addElement(new StringBuffer(String.valueOf(ScreenDapDo.selectGemBH.lock ? 1 : 0)).toString());
                    ScreenDapDo.BHLock = ScreenDapDo.selectGemBH.lock;
                    ScreenDapDo.idBH = ScreenDapDo.selectGemBH.id;
                } else {
                    idgem.addElement("-1");
                    lock.addElement("0");
                    ScreenDapDo.BHLock = false;
                    ScreenDapDo.idBH = (short) -1;
                }
                if (ScreenDapDo.selectGemMM != null) {
                    idgem.addElement(new StringBuffer(String.valueOf((int) ScreenDapDo.selectGemMM.id)).toString());
                    lock.addElement(new StringBuffer(String.valueOf(ScreenDapDo.selectGemMM.lock ? 1 : 0)).toString());
                    ScreenDapDo.MMLock = ScreenDapDo.selectGemMM.lock;
                    ScreenDapDo.idMM = ScreenDapDo.selectGemMM.id;
                } else {
                    idgem.addElement("-1");
                    lock.addElement("0");
                    ScreenDapDo.MMLock = false;
                    ScreenDapDo.idMM = (short) -1;
                }
                ScreenDapDo.itemID = ScreenDapDo.itemSelect.itemID;
                Vector menu = new Vector();
                for (int i = 1; i <= 15; i++) {
                    final int plus = i;
                    menu.addElement(new Command(new StringBuffer("Cộng ").append(i).toString(), new IAction() {
                        @Override
                        public void perform() {
                            GameService.gI().doAutoImbue(ScreenDapDo.itemSelect.itemID, idgem, plus, lock, -1);
                            ScreenDapDo.this.isShowPopup = true;
                            ScreenDapDo.this.infoPoup = "Xin chờ";
                            ScreenDapDo.isFinishImbue = false;
                            Canvas.startOKDlg("Xin chờ");
                        }
                    }));
                }
                Canvas.menu.startAt(menu, 3);
                Canvas.currentDialog = null;
                return;
            }
            Canvas.startOKDlg("Chưa đặt vật phẩm.", new IAction() {
                @Override
                public void perform() {
                    ScreenDapDo.this.right.action.perform();
                    Canvas.endDlg();
                }
            });
        }
    }

    protected void doEp() {
        int xbg = (xDr + (wCell / 2)) - 44;
        int i = (yDr + (hCell / 2)) - 16;
        if (Canvas.isSmartPhone) {
            xbg = (xDr + (wCell / 4)) - 44;
        }
        int[] iArr = this.xTo;
        int i2 = (xbg + 44) - 11;
        this.xTo[1] = i2;
        iArr[0] = i2;
        this.cout = 10;
        this.isEp = true;
    }

    public void updatEp() {
        this.cout--;
        if (this.cout < 0) {
            this.cout = 0;
        }
        if (this.cout > 0) {
            return;
        }
        if (this.xItem[0] != this.xTo[0]) {
            if (((this.xTo[0] - this.xItem[0]) >> 1) == 0) {
                this.xItem[0] = this.xTo[0];
            } else {
                int[] iArr = this.xItem;
                iArr[0] = iArr[0] + ((this.xTo[0] - this.xItem[0]) >> 1);
            }
        }
        if (this.xItem[1] != this.xTo[1]) {
            if (((this.xTo[1] - this.xItem[1]) >> 1) == 0) {
                this.xItem[1] = this.xTo[1];
            } else {
                int[] iArr2 = this.xItem;
                iArr2[1] = iArr2[1] + ((this.xTo[1] - this.xItem[1]) >> 1);
            }
        }
    }

    protected void doView() {
        if (this.currentRow == 1) {
            if (Char.inventory.size() == 0 || this.selectItemInventory < 0 || this.selectItemInventory > listItem.size() - 1) {
                return;
            }
            ItemInInventory item = (ItemInInventory) listItem.elementAt(this.selectItemInventory);
            if (!item.isEnoughData) {
                item.isEnoughData = true;
                GameService.gI().requestItemInfo(item.itemID, Canvas.gameScr.mainChar.ID);
            }
            this.arrShowText = Font.normalFont[0].splitFontBStrInLine(item.getDescription(false), this.wSh - 10);
        } else if (this.currentRow == 0) {
            if (vnguyenLieu.size() <= 0) {
                return;
            }
            GemTemplate gem = (GemTemplate) vnguyenLieu.elementAt(this.indexTabNguyenLieu);
            GemTemp g = Res.getGemByID(gem.id);
            String itemInfo = ItemInInventory.setTemp(g.name, "0");
            this.arrShowText = Font.normalFont[0].splitFontBStrInLine(new StringBuffer(String.valueOf(itemInfo)).append(ItemInInventory.setTemp(g.decript, "0")).toString(), this.wSh - 10);
        }
        if (!Canvas.isSmartPhone) {
            if (this.arrShowText.length == 1) {
                this.hSh = 35;
                this.ySh = (Canvas.h / 2) - (Font.normalFont[0].getHeight() / 2);
            } else {
                this.hSh = (this.arrShowText.length * Screen.ITEM_HEIGHT) + 8;
                this.ySh = (Canvas.h / 2) - ((this.arrShowText.length * (Screen.ITEM_HEIGHT - 3)) / 2);
            }
            if (this.ySh < yDr) {
                this.ySh = yDr;
            }
            if (this.hSh > 150) {
                this.hSh = 150;
            }
            this.xSh = xDr;
        }
        this.isShowText = true;
        if (!Canvas.isSmartPhone) {
            this.center = this.cmdErr;
        }
        this.left = this.cmdErr;
    }

    public void paintCell(Graphics g) {
        g.setColor(game.pokemon.GiaoDien.mauKhungCon());
        int h0 = Canvas.isSmartPhone ? -27 : 10;
        g.fillRect(this.xSh + 2, this.ySh + h0, this.wSh, this.hSh - 8);
        g.setColor(Font.nenSang ? game.pokemon.GiaoDien.XANH : game.pokemon.GiaoDien.VANG);
        g.drawRect(this.xSh + 2, this.ySh + h0, this.wSh, this.hSh - 8);
    }

    public void paintShowText(Graphics g) {
        Canvas.resetTrans(g);
        if (wCell >= 300) {
            paintCell(g);
            if (!this.isShowText) {
                return;
            }
        } else if (!this.isShowText) {
            return;
        } else {
            paintCell(g);
        }
        int ho = Canvas.isSmartPhone ? 24 : 0;
        this.mScrollShowText.setStyle(this.arrShowText.length, Screen.ITEM_HEIGHT - 2, this.xSh, this.ySh - ho, this.wSh - 2, this.hSh - 10, true, 0);
        this.mScrollShowText.setClip(g, this.xSh, (this.ySh + (Canvas.isSmartPhone ? 2 : 10)) - ho, this.wSh - 2, this.hSh - 15);
        for (int i = 0; i < this.arrShowText.length; i++) {
            if (!this.arrShowText[i].equals("")) {
                byte color = (byte) (this.arrShowText[i].charAt(0) - '0');
                byte indexsub = 1;
                if (!WindowInfoScr.isDegit(this.arrShowText[i].charAt(0))) {
                    color = 0;
                    indexsub = 0;
                }
                Font.normalFont[color >= 5 ? (byte) 0 : color].drawString(g, this.arrShowText[i].substring(indexsub), this.xSh + 8, ((this.ySh + (i * (Screen.ITEM_HEIGHT - 3))) + (Canvas.isSmartPhone ? 6 : 14)) - (ho + (Canvas.isSmartPhone ? 2 : 0)), 0);
            }
        }
    }

    @Override
    public void paint(Graphics g) {
        if (this.last != null) {
            this.last.paint(g);
        }
        int i = (Canvas.isSmartPhone && wCell == 300) ? wCell / 2 : wCell;
        Res.paintDlgFull(g, xDr, yDr, wCell, hCell, 25, this.tile, true, this.h0 - 28);
        Canvas.resetTrans(g);
        this.mScrollItemInventory.setStyle(Char.inventory.size(), this.wTab, xDr, yDr + 28 + this.h0, wCell - 2, 27, false, 0);
        this.mScrollItemInventory.setClip(g, xDr + 2, yDr + 23 + this.h0, wCell - 4, 31);
        for (int i2 = 0; i2 < listItem.size(); i2++) {
            if (this.selectItemInventory == i2 && this.currentRow == 1) {
                g.setColor(0x3D7DCA);
                g.fillRect(xDr + (this.wTab * i2), yDr + 23 + this.h0, this.wTab, 28);
            }
            ItemInInventory item = (ItemInInventory) listItem.elementAt(i2);
            item.paint(g, xDr + (this.wTab * i2) + (this.wTab / 2), yDr + 36 + this.h0);
            g.setColor(0x6FA6EA);
            g.fillRect(xDr + (this.wTab * i2), yDr + 22 + this.h0, 1, 29);
            if (itemSelect != null && itemSelect.itemID == item.itemID) {
                g.setColor(Font.nenSang ? game.pokemon.GiaoDien.XANH : game.pokemon.GiaoDien.VANG);
                g.drawRect(xDr + (this.wTab * i2) + 1, yDr + 23 + this.h0, this.wTab - 2, 28);
                g.drawRect(xDr + (this.wTab * i2) + 2, yDr + 24 + this.h0, this.wTab - 4, 26);
            }
        }
        g.setColor(0x6FA6EA);
        g.fillRect(xDr + (this.wTab * listItem.size()), yDr + 22 + this.h0, 1, 29);
        Canvas.resetTrans(g);
        int wi = !Canvas.isSmartPhone ? wCell - 2 : (wCell - 2) - this.wSh;
        this.mScrollNguyenLieu.setStyle(vnguyenLieu.size(), this.wTab + 2, xDr, yDr + this.h0, wi, 27, false, 0);
        this.mScrollNguyenLieu.setClip(g, xDr + 2, ((yDr + 23) + this.h0) - 28, wi - 2, 31);
        for (int i3 = 0; i3 < vnguyenLieu.size(); i3++) {
            if (this.indexTabNguyenLieu == i3 && this.currentRow == 0) {
                g.setColor(0x3D7DCA);
                g.fillRect(xDr + (this.wTab * i3), ((yDr + 24) + this.h0) - 28, this.wTab, 26);
            }
            GemTemplate gem = (GemTemplate) vnguyenLieu.elementAt(i3);
            if (gem.lock) {
                g.setColor(3276851);
                g.fillRect(xDr + (this.wTab * i3), ((yDr + 24) + this.h0) - 28, this.wTab, 26);
                if (this.indexTabNguyenLieu == i3 && this.currentRow == 0) {
                    g.setColor(0x3D7DCA);
                    g.fillRect(xDr + (this.wTab * i3), ((yDr + 24) + this.h0) - 28, this.wTab, 26);
                }
            }
            if (selectGem != null && selectGem.id == gem.id && selectGem.lock == gem.lock) {
                g.setColor(Font.nenSang ? game.pokemon.GiaoDien.XANH : game.pokemon.GiaoDien.VANG);
                g.drawRect(xDr + (this.wTab * i3) + 1, ((yDr + 24) + this.h0) - 28, this.wTab - 2, 25);
                g.drawRect(xDr + (this.wTab * i3) + 2, ((yDr + 24) + this.h0) - 27, this.wTab - 4, 23);
            }
            if (selectGemBH != null && selectGemBH.id == gem.id && selectGemBH.lock == gem.lock) {
                g.setColor(238149631);
                g.drawRect(xDr + (this.wTab * i3) + 1, ((yDr + 24) + this.h0) - 28, this.wTab - 2, 25);
                g.drawRect(xDr + (this.wTab * i3) + 2, ((yDr + 24) + this.h0) - 27, this.wTab - 4, 23);
            }
            if (selectGemMM != null && selectGemMM.id == gem.id && selectGemMM.lock == gem.lock) {
                g.setColor(238149631);
                g.drawRect(xDr + (this.wTab * i3) + 1, ((yDr + 24) + this.h0) - 28, this.wTab - 2, 25);
                g.drawRect(xDr + (this.wTab * i3) + 2, ((yDr + 24) + this.h0) - 27, this.wTab - 4, 23);
            }
            Res.paintGem(g, Res.getGemByID(gem.id).idImage, xDr + (this.wTab * i3) + (this.wTab / 2), (((yDr + 24) + this.h0) - 28) + 13);
            Font.smallFontColor[3].drawString(g, new StringBuffer(String.valueOf(gem.number)).toString(), ((xDr + (this.wTab * i3)) + this.wTab) - 1, ((yDr + 22) + this.h0) - Font.smallFontColor[3].getHeight(), 1);
            g.setColor(0x6FA6EA);
            g.fillRect(xDr + (this.wTab * i3), ((yDr + 22) + this.h0) - 28, 1, 29);
        }
        g.fillRect(xDr + (this.wTab * vnguyenLieu.size()), ((yDr + 22) + this.h0) - 28, 1, 28);
        Canvas.resetTrans(g);
        paintItemEp(g);
        paintShowText(g);
        super.paint(g);
    }

    public void paintItemEp(Graphics g) {
        for (int i = 0; i < this.xItem.length; i++) {
            g.setColor(16777215);
            g.fillRect(this.xItem[i], this.yItem[i], 22, 22);
            if (this.cout % 2 == 0) {
                g.setColor(0x3D7DCA);
                g.fillRect(this.xItem[i] + 1, this.yItem[i] + 1, 20, 20);
            }
        }
        if (this.type != 1) {
            if (selectGem != null) {
                Res.paintGem(g, Res.getGemByID(selectGem.id).idImage, this.xItem[1] + 10, this.yItem[1] + 10);
            }
            if (itemSelect != null) {
                itemSelect.paint(g, this.xItem[0] + 10, this.yItem[0] + 10);
            }
            if (this.type == 4 && selectGemBH != null) {
                Res.paintGem(g, Res.getGemByID(selectGemBH.id).idImage, this.xItem[2] + 10, this.yItem[2] + 10);
            }
        } else if (this.type == 1) {
            if (selectGem != null) {
                Res.paintGem(g, Res.getGemByID(selectGem.id).idImage, this.xItem[2] + 10, this.yItem[2] + 10);
            }
            if (selectGemBH != null) {
                Res.paintGem(g, Res.getGemByID(selectGemBH.id).idImage, this.xItem[1] + 10, this.yItem[1] + 10);
            }
            if (selectGemMM != null) {
                Res.paintGem(g, Res.getGemByID(selectGemMM.id).idImage, this.xItem[3] + 10, this.yItem[3] + 10);
            }
            if (itemSelect != null) {
                itemSelect.paint(g, this.xItem[0] + 10, this.yItem[0] + 10);
            }
        }
        if (this.type == 0 || !this.isShowPopup || this.infoPoup.equals("")) {
            return;
        }
        int y00 = (yDr + this.h0) - 24;
        g.setColor(16777215);
        g.fillRect(this.xPopup - 60, y00 - 3, 120, 20);
        g.setColor(3222312);
        g.fillRect(this.xPopup - 59, y00 - 2, 118, 18);
        Font.normalFont[0].drawString(g, this.infoPoup, this.xPopup, y00, 2);
    }

    @Override
    public void update() {
        if (this.last != null) {
            this.last.update();
        }
        updatEp();
        if (this.timeDelay > 0) {
            this.timeDelay = (byte) (this.timeDelay - 1);
        }
        if (this.timeDelay <= 0) {
            this.timeDelay = (byte) 0;
        }
        boolean isFocus = false;
        boolean isFocus1 = false;
        if (Canvas.currentDialog == null) {
            if (Canvas.isSmartPhone) {
                if (this.mScrollShowText.FocusNew) {
                    this.mScrollItemInventory.FocusNew = false;
                    this.mScrollNguyenLieu.FocusNew = false;
                } else if (this.mScrollItemInventory.FocusNew) {
                    this.mScrollShowText.FocusNew = false;
                    this.mScrollNguyenLieu.FocusNew = false;
                } else if (this.mScrollNguyenLieu.FocusNew) {
                    this.mScrollItemInventory.FocusNew = false;
                    this.mScrollShowText.FocusNew = false;
                }
                if (this.mScrollItemInventory.FocusNew && !Canvas.menu.showMenu && this.timeDelay == 0) {
                    this.currentRow = (byte) 1;
                    ScrollResult r0 = this.mScrollItemInventory.updateKey();
                    if (r0.isDowning || r0.isFinish) {
                        this.selectItemInventory = r0.selected;
                        this.lastIndex1 = r0.selected;
                        isFocus = true;
                        if (this.right != null && this.isShowText && Canvas.canTouch()) {
                            this.right.action.perform();
                        }
                    }
                }
                this.mScrollItemInventory.updatecm();
                if (this.mScrollNguyenLieu.FocusNew && !Canvas.menu.showMenu && this.timeDelay == 0) {
                    this.currentRow = (byte) 0;
                    ScrollResult r1 = this.mScrollNguyenLieu.updateKey();
                    if (r1.isDowning || r1.isFinish) {
                        this.indexTabNguyenLieu = r1.selected;
                        this.lastIndex2 = r1.selected;
                        isFocus1 = true;
                        if (this.right != null && this.isShowText && Canvas.canTouch()) {
                            this.right.action.perform();
                        }
                    }
                }
                this.mScrollNguyenLieu.updatecm();
                if (this.mScrollShowText.FocusNew && !Canvas.menu.showMenu) {
                    this.mScrollShowText.updateKey();
                }
                this.mScrollShowText.updatecm();
                if (!Canvas.isPointerDown) {
                    if (this.mScrollItemInventory.FocusNew && isFocus) {
                        if (this.selectItemInventory > -1 && this.selectItemInventory <= listItem.size() - 1) {
                            if (this.lastIndex1 != -1) {
                                this.selectItemInventory = this.lastIndex1;
                            }
                            this.cmdView.action.perform();
                        }
                    } else if (this.mScrollNguyenLieu.FocusNew && isFocus1 && this.indexTabNguyenLieu > -1 && this.indexTabNguyenLieu <= vnguyenLieu.size() - 1) {
                        if (this.lastIndex2 != -1) {
                            this.indexTabNguyenLieu = this.lastIndex2;
                        }
                        this.cmdView.action.perform();
                    }
                    this.mScrollShowText.FocusNew = false;
                    this.mScrollItemInventory.FocusNew = false;
                    this.mScrollNguyenLieu.FocusNew = false;
                }
            } else if (!this.isShowText) {
                ScrollResult r2 = this.mScrollItemInventory.updateKey();
                if (r2.isDowning || r2.isFinish) {
                    this.selectItemInventory = r2.selected;
                    this.mScrollItemInventory.moveTo(this.selectItemInventory * (this.wTab + 2));
                }
                this.mScrollItemInventory.updatecm();
                ScrollResult r01 = this.mScrollNguyenLieu.updateKey();
                if (r01.isDowning || r01.isFinish) {
                    this.indexTabNguyenLieu = r01.selected;
                    this.mScrollNguyenLieu.moveTo(this.indexTabNguyenLieu * (this.wTab + 2));
                }
                this.mScrollNguyenLieu.updatecm();
            } else {
                this.mScrollShowText.updateKey();
                this.mScrollShowText.updatecm();
            }
        }
        super.update();
    }

    public static boolean isPointer(int x, int y, int w, int h) {
        if (Canvas.isPointerDown) {
            if (Canvas.px >= x && Canvas.px <= x + w && Canvas.py >= y && Canvas.py <= y + h) {
                return false;
            }
            return true;
        }
        return false;
    }

    @Override
    public void updateKey() {
        if (this.isShowText) {
            if (Canvas.keyPressed[2]) {
                Canvas.keyPressed[2] = false;
                this.mScrollShowText.cmtoY -= 50;
                if (this.mScrollShowText.cmtoY < 0) {
                    this.mScrollShowText.cmtoY = 0;
                }
            } else if (Canvas.keyPressed[8]) {
                Canvas.keyPressed[8] = false;
                this.mScrollShowText.cmtoY += 50;
                if (this.mScrollShowText.cmtoY > this.mScrollShowText.cmyLim) {
                    this.mScrollShowText.cmtoY = this.mScrollShowText.cmyLim;
                }
            }
        } else {
            if (Canvas.keyPressed[2]) {
                Canvas.keyPressed[2] = false;
                this.currentRow = (byte) (this.currentRow - 1);
                if (this.currentRow < 0) {
                    this.currentRow = (byte) 0;
                }
            } else if (Canvas.keyPressed[8]) {
                Canvas.keyPressed[8] = false;
                this.currentRow = (byte) (this.currentRow + 1);
                if (this.currentRow > 1) {
                    this.currentRow = (byte) 1;
                }
            }
            if (Canvas.keyPressed[4]) {
                Canvas.keyPressed[4] = false;
                if (this.currentRow == 1) {
                    this.selectItemInventory--;
                    if (this.selectItemInventory < 0) {
                        this.selectItemInventory = listItem.size() - 1;
                    }
                    this.mScrollItemInventory.moveTo(this.selectItemInventory * this.wTab);
                } else if (this.currentRow == 0) {
                    this.indexTabNguyenLieu--;
                    if (this.indexTabNguyenLieu < 0) {
                        this.indexTabNguyenLieu = vnguyenLieu.size() - 1;
                    }
                    this.mScrollNguyenLieu.moveTo(this.indexTabNguyenLieu * this.wTab);
                } else {
                    this.selectItem--;
                    if (this.selectItem < 0) {
                        this.selectItem = this.xItem.length - 1;
                    }
                }
            } else if (Canvas.keyPressed[6]) {
                Canvas.keyPressed[6] = false;
                if (this.currentRow == 1) {
                    this.selectItemInventory++;
                    if (this.selectItemInventory > listItem.size() - 1) {
                        this.selectItemInventory = 0;
                    }
                    this.mScrollItemInventory.moveTo(this.selectItemInventory * this.wTab);
                } else if (this.currentRow == 0) {
                    this.indexTabNguyenLieu++;
                    if (this.indexTabNguyenLieu > vnguyenLieu.size() - 1) {
                        this.indexTabNguyenLieu = 0;
                    }
                    this.mScrollNguyenLieu.moveTo(this.indexTabNguyenLieu * this.wTab);
                } else {
                    this.selectItem--;
                    if (this.selectItem < 0) {
                        this.selectItem = this.xItem.length - 1;
                    }
                }
            }
        }
        super.updateKey();
    }

    public static boolean isBH(int id) {
        return id >= 0 && id <= 4;
    }

    public static boolean isDaMayMan(int id) {
        return (id >= 5 && id <= 7) || id == 155 || id == 156;
    }

    public static boolean isLKD(int id) {
        return (id >= 8 && id <= 11) || id == 66 || id == 245 || id == 157 || id == 158;
    }

    public static boolean isBot(int type) {
        return type == GemTemp.TYPE_BOT;
    }

    public boolean isDaTienCap(int type) {
        return type == GemTemp.TYPE_DA_TIEN_GIAI;
    }

    public void getListGem(boolean getItem, boolean recheck) {
        vnguyenLieu.removeAllElements();
        if (getItem) {
            listItem.removeAllElements();
        }
        Vector v = MainChar.gemItem;
        int size0 = v.size();
        int check0 = 0;
        int check1 = 0;
        int check2 = 0;
        for (int i = 0; i < size0; i++) {
            GemTemplate gem = (GemTemplate) v.elementAt(i);
            GemTemp g = Res.getGemByID(gem.id);
            if (this.type == 0) {
                if (g.type == GemTemp.TYPE_ADD_NEW_ATTRIBUTE) {
                    if (selectGem != null && selectGem.id == gem.id && !selectGem.lock) {
                        selectGem = gem;
                        check0 = 1;
                    }
                    vnguyenLieu.addElement(gem);
                }
            } else if (this.type == 2) {
                if (g.type == GemTemp.TYPE_LOCK_ANIMAL) {
                    if (selectGem != null && selectGem.id == gem.id && !selectGem.lock) {
                        selectGem = gem;
                    }
                    vnguyenLieu.addElement(gem);
                }
            } else if (this.type == 3) {
                if (g.type == GemTemp.TYPE_LOCK_ITEM) {
                    if (selectGem != null && selectGem.id == gem.id && !selectGem.lock) {
                        selectGem = gem;
                    }
                    vnguyenLieu.addElement(gem);
                }
            } else if (this.type == 4) {
                if (g.type == GemTemp.TYPE_BOT || g.type == GemTemp.TYPE_DA_TIEN_GIAI) {
                    if (selectGem != null && selectGem.id == gem.id && !selectGem.lock && g.type == GemTemp.TYPE_BOT) {
                        selectGem = gem;
                        check0 = 1;
                    }
                    if (selectGemBH != null && selectGemBH.id == gem.id && !selectGemBH.lock && g.type == GemTemp.TYPE_DA_TIEN_GIAI) {
                        selectGemBH = gem;
                        check1 = 1;
                    }
                    vnguyenLieu.addElement(gem);
                }
            } else if (g.type == GemTemp.TYPE_IMBUE) {
                if (selectGem != null && selectGem.id == gem.id && !selectGem.lock) {
                    selectGem = gem;
                    check0 = 1;
                } else if (selectGem == null && recheck && idLkd == gem.id && !LKDLock) {
                    selectGem = gem;
                    check0 = 1;
                }
                if (selectGemBH != null && selectGemBH.id == gem.id && !selectGemBH.lock) {
                    selectGemBH = gem;
                    check1 = 1;
                } else if (selectGemBH == null && recheck && idBH == gem.id && !BHLock) {
                    selectGemBH = gem;
                    check1 = 1;
                }
                if (selectGemMM != null && selectGemMM.id == gem.id && !selectGemMM.lock) {
                    selectGemMM = gem;
                    check2 = 1;
                } else if (selectGemMM == null && recheck && idMM == gem.id && !MMLock) {
                    selectGemMM = gem;
                    check2 = 1;
                }
                vnguyenLieu.addElement(gem);
            }
        }
        Vector v2 = MainChar.gemItemKhoa;
        int size1 = v2.size();
        int checkLock0 = 0;
        int checkLock1 = 0;
        int checkLock2 = 0;
        for (int i2 = 0; i2 < size1; i2++) {
            GemTemplate gem2 = (GemTemplate) v2.elementAt(i2);
            GemTemp g2 = Res.getGemByID(gem2.id);
            if (this.type == 0) {
                if (g2.type == GemTemp.TYPE_ADD_NEW_ATTRIBUTE) {
                    if (selectGem != null && selectGem.id == gem2.id && selectGem.lock) {
                        selectGem = gem2;
                        checkLock0 = 1;
                    }
                    vnguyenLieu.addElement(gem2);
                    gem2.lock = true;
                }
            } else if (this.type == 2) {
                if (g2.type == GemTemp.TYPE_LOCK_ANIMAL) {
                    if (selectGem != null && selectGem.id == gem2.id && selectGem.lock) {
                        selectGem = gem2;
                    }
                    vnguyenLieu.addElement(gem2);
                    gem2.lock = true;
                }
            } else if (this.type == 3) {
                if (g2.type == GemTemp.TYPE_LOCK_ITEM) {
                    if (selectGem != null && selectGem.id == gem2.id && selectGem.lock) {
                        selectGem = gem2;
                    }
                    vnguyenLieu.addElement(gem2);
                    gem2.lock = true;
                }
            } else if (this.type == 4) {
                if (g2.type == GemTemp.TYPE_BOT || g2.type == GemTemp.TYPE_DA_TIEN_GIAI) {
                    if (selectGem != null && selectGem.id == gem2.id && selectGem.lock && g2.type == GemTemp.TYPE_BOT) {
                        selectGem = gem2;
                        checkLock0 = 1;
                    }
                    if (selectGemBH != null && selectGemBH.id == gem2.id && selectGemBH.lock && g2.type == GemTemp.TYPE_DA_TIEN_GIAI) {
                        selectGemBH = gem2;
                        checkLock1 = 1;
                    }
                    vnguyenLieu.addElement(gem2);
                    gem2.lock = true;
                }
            } else if (g2.type == GemTemp.TYPE_IMBUE) {
                if (selectGem != null && selectGem.id == gem2.id && selectGem.lock) {
                    selectGem = gem2;
                    checkLock0 = 1;
                } else if (selectGem == null && recheck && idLkd == gem2.id && LKDLock) {
                    selectGem = gem2;
                    checkLock0 = 1;
                }
                if (selectGemBH != null && selectGemBH.id == gem2.id && selectGemBH.lock) {
                    selectGemBH = gem2;
                    checkLock1 = 1;
                } else if (selectGemBH == null && recheck && idBH == gem2.id && BHLock) {
                    selectGemBH = gem2;
                    checkLock1 = 1;
                }
                if (selectGemMM != null && selectGemMM.id == gem2.id && selectGemMM.lock) {
                    selectGemMM = gem2;
                    checkLock2 = 1;
                } else if (selectGemMM == null && recheck && idMM == gem2.id && MMLock) {
                    selectGemMM = gem2;
                    checkLock2 = 1;
                }
                vnguyenLieu.addElement(gem2);
                gem2.lock = true;
            }
        }
        if (this.type == 0) {
            if (check0 == 0 && checkLock0 == 0) {
                selectGem = null;
            }
            if (check1 == 0 && checkLock1 == 0) {
                selectGemBH = null;
            }
            if (check2 == 0 && checkLock2 == 0) {
                selectGemMM = null;
            }
        }
        boolean napDanhSach = true;
        if (napDanhSach) {
            if (this.type == 0 || this.type == 4) {
                listItem = ItemInInventory.getAllItem(COLOR_GREEN, COLOR_MAGENTA);
            } else if (this.type == 2) {
                listItem = ItemInInventory.getAllItemAnimal(COLOR_GREEN, 1);
            } else if (this.type == 3) {
                listItem = ItemInInventory.getAllItemCharExcepColor(COLOR_WHITE);
            } else {
                listItem = ItemInInventory.getAllItem();
            }
        }
        for (int i3 = 0; i3 < listItem.size(); i3++) {
            ItemInInventory item = (ItemInInventory) listItem.elementAt(i3);
            if (itemID == item.itemID) {
                itemSelect = item;
                return;
            }
        }
    }

    public void setInfoItem(int itemid) {
        ItemInInventory it;
        if (this.selectItemInventory < listItem.size() && (it = (ItemInInventory) listItem.elementAt(this.selectItemInventory)) != null && it.itemID == itemid) {
            this.arrShowText = Font.normalFont[0].splitFontBStrInLine(it.getDescription(false), this.wSh - 10);
        }
    }

    public void doFinishInbue() {
        getListGem(false, true);
        this.isShowPopup = true;
    }

    public void resetSelect() {
        selectGem = null;
        selectGemBH = null;
        selectGemMM = null;
        itemSelect = null;
        getListGem(true, false);
    }

    @Override
    public void switchToMe() {
        super.switchToMe();
    }

    @Override
    public void switchToMe(Screen lastSCR) {
        this.last = lastSCR;
        super.switchToMe(lastSCR);
    }
}
