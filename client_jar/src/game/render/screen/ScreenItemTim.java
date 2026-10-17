package game.render.screen;

import game.model.Char;
import game.model.Command;
import game.model.Cout;
import game.model.Effect;
import game.model.GemTemp;
import game.model.GemTemplate;
import game.model.IAction;
import game.model.ItemInInventory;
import game.model.MainChar;
import game.model.Scroll;
import game.model.ScrollResult;
import game.model.arrow.Arrow;
import game.model.arrow.WeaponsLazer;
import game.networklogic.GameService;
import game.render.Canvas;
import game.render.Res;
import game.render.Util;
import java.util.Vector;
import javax.microedition.lcdui.Graphics;
import network.Message;

public class ScreenItemTim extends Screen {
    public static ScreenItemTim me;
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
    public static final byte TypeXayBot = 5;
    public static final byte TYPE_ANIMAL = 2;
    public static final byte TYPE_LOCK_ITEM = 3;
    public static final byte TYPE_EP_ITEM_TIM = 6;
    public byte type;
    public byte frame;
    public byte selectMenuItem;
    public boolean createBot;
    public boolean isdestroyitem;
    public boolean isXayBot;
    public boolean isShot;
    public boolean isDap;
    public int xto;
    public int yto;
    public int hclip;
    public int hclip1;
    Command cmdClose;
    Command cmdErr;
    Command cmdView;
    Command cmdContinue;
    Command cmdXay;
    Command cmdWaiting;
    Command cmdDapItemTim;
    int cout;
    static ItemInInventory itemSelect;
    public ItemInInventory selecItemNL1;
    public ItemInInventory selecItemNL3;
    public ItemInInventory selecItemNL4;
    public ItemInInventory selecItemNL6;
    int[] xItem;
    int[] yItem;
    int[] xTo;
    int[] yTo;
    boolean isEp;
    boolean isShowPopup;
    int xPopup;
    int yPopup;
    boolean DropItem;
    public int tick;
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
    static GemTemplate selecItemNL2 = null;
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
    public Vector mShot = new Vector();
    public int size = 22;
    public Vector ListTtem = new Vector();
    public boolean[] CheckHaveItemCell = new boolean[6];
    public boolean[] CheckPaintItem = new boolean[6];
    public Vector LisItemSelect = new Vector();
    public short id_bot = 0;
    public byte typeResult = 0;
    public byte soluong = 1;
    public Vector mEffect = new Vector();
    public Vector lazer = new Vector();
    String infoPoup = "Thong bao";
    Vector mDestroy = new Vector();
    public int timeDlay = 15;
    public int dem = -1;
    public byte currentCell = 5;

    public static ScreenItemTim gI() {
        if (me != null) {
            return me;
        }
        ScreenItemTim screenItemTim = new ScreenItemTim();
        me = screenItemTim;
        return screenItemTim;
    }

    public void setData(Message msg) {
    }

    public void resetData() {
        this.mScrollItemInventory.clear();
        this.LisItemSelect.removeAllElements();
        this.mScrollNguyenLieu.clear();
        this.mScrollShowText.clear();
        this.mEffect.removeAllElements();
        this.lazer.removeAllElements();
        this.mShot.removeAllElements();
        vnguyenLieu.removeAllElements();
        this.index = Canvas.isSmartPhone ? -1 : 0;
        this.selectItemInventory = 0;
        this.indexTabNguyenLieu = 0;
        this.isShowPopup = false;
        this.createBot = false;
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
        this.type = type;
        int xbg = (xDr + (wCell / 2)) - 44;
        int ybg = (yDr + (hCell / 2)) - 35;
        if (Canvas.isSmartPhone) {
            xbg = (xDr + (wCell / 4)) - 44;
        }
        if (type == 6) {
            this.currentRow = (byte) 1;
            this.cmdDapItemTim.caption = "Đập";
            this.tile = "Đập đồ tím";
            int y0 = yDr + 27 + ((this.h0 - 28) / 2);
            int r = hCell / 4;
            int radia = 30;
            this.xItem = new int[7];
            this.yItem = new int[7];
            this.xItem[0] = (xbg + 44) - 10;
            this.yItem[0] = (y0 - (this.size / 2)) + 20;
            this.currentRow = (byte) -1;
            for (int i = 1; i < 7; i++) {
                this.xItem[i] = ((Util.cos(radia) * r) / 1024) + this.xItem[0];
                this.yItem[i] = ((Util.sin(radia) * r) / 1024) + this.yItem[0];
                radia += 60;
            }
        } else if (type == 5) {
            int y1 = yDr + 27 + ((this.h0 - 28) / 2);
            this.cmdXay.caption = "Nghiền";
            this.tile = "Nghiền Bột";
            this.currentRow = (byte) 1;
            this.xItem = new int[5];
            this.xTo = new int[4];
            this.yTo = new int[4];
            this.yItem = new int[5];
            this.xItem[0] = (xbg + 44) - 11;
            this.xItem[1] = this.xItem[0] - 48;
            this.xItem[3] = this.xItem[0] + 48;
            this.xItem[2] = this.xItem[0];
            this.yItem[0] = (y1 - (this.size / 2)) + 3;
            if (this.yItem[0] - (2 * this.size) < yDr + 27) {
                this.yItem[0] = yDr + 27 + (2 * this.size);
            }
            this.yItem[2] = this.yItem[0] - (2 * this.size);
            this.yItem[1] = this.yItem[0] + 28;
            this.yItem[3] = this.yItem[0] + 28;
            this.xTo[0] = this.xItem[0];
            this.xTo[1] = this.xItem[1];
            this.xTo[2] = this.xItem[2];
            this.xTo[3] = this.xItem[3];
            this.yTo[0] = this.yItem[0];
            this.yTo[1] = this.yItem[1];
            this.yTo[2] = this.yItem[2];
            this.yTo[3] = this.yItem[3];
            this.xPopup = (xbg + 44) - 1;
            this.yPopup = ybg + 40;
            if (Canvas.isSmallScreen) {
                this.yItem[2] = (this.yItem[0] - (2 * this.size)) + 4;
                this.xItem[4] = this.xItem[0];
                this.yItem[4] = this.yItem[0] + 40;
            } else {
                this.xItem[4] = this.xItem[0];
                this.yItem[4] = this.yItem[0] + 50;
            }
            this.xto = this.xTo[0];
            this.yto = this.yTo[0];
            this.hclip = this.size;
            this.hclip1 = 0;
        }
        Cout.println(new StringBuffer("TYPE SCREENDAPDO ").append((int) type).toString());
        this.cout = 0;
        if (type == 5) {
            if (isContinue) {
                this.currentRow = (byte) 0;
                if (Canvas.isSmartPhone) {
                    this.currentRow = (byte) -1;
                }
                this.left = this.cmdXay;
                this.isShowText = false;
                this.mScrollShowText.clear();
                this.isEp = false;
            } else {
                this.isEp = false;
                this.left = this.cmdXay;
            }
        } else if (type == 6) {
            if (isContinue) {
                this.currentRow = (byte) 0;
                if (Canvas.isSmartPhone) {
                    this.currentRow = (byte) -1;
                }
                this.left = this.cmdDapItemTim;
                this.isShowText = false;
                this.mScrollShowText.clear();
                this.isEp = false;
            } else {
                this.isEp = false;
                this.left = this.cmdDapItemTim;
            }
        }
        resetSelect();
        getListGem(true, false);
        if (type == 6) {
            addNguyenlieu();
        }
    }

    public ScreenItemTim() {
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
        this.cmdDapItemTim = new Command("Đập", new IAction() {
            @Override
            public void perform() {
                if (!ScreenItemTim.this.CheckHaveItemcell()) {
                    Canvas.startOKDlg("Bạn chưa bỏ nguyên liệu vào ô!");
                } else {
                    if (!ScreenItemTim.this.CheckHaveItemcell() || ScreenItemTim.this.isDap) {
                        return;
                    }
                    ScreenItemTim.this.addlazeEpItemTim();
                    ScreenItemTim.this.isShot = true;
                    ScreenItemTim.this.isDap = true;
                }
            }
        });
        this.cmdWaiting = new Command("Xin chờ", new IAction() {
            @Override
            public void perform() {
            }
        });
        this.cmdXay = new Command("Nghiền", new IAction() {
            @Override
            public void perform() {
                if (ScreenItemTim.itemSelect == null) {
                    Canvas.startOKDlg("Bạn chưa bỏ trang bị vào");
                } else if (!ScreenItemTim.this.isXayBot) {
                    GameService.gI().doRequestTachNguyenLIeu(ScreenItemTim.itemSelect.itemID, 4, 0, 0);
                    Canvas.startOKDlg("Xin chờ");
                }
            }
        });
        this.cmdClose = new Command("Đóng", new IAction() {
            @Override
            public void perform() {
                if (ScreenItemTim.this.isShowText) {
                    ScreenItemTim.this.isShowText = false;
                    ScreenItemTim.this.center = ScreenItemTim.this.cmdView;
                    ScreenItemTim.this.mScrollShowText.clear();
                    ScreenItemTim.this.arrShowText = new String[]{""};
                    if (ScreenItemTim.this.isEp) {
                        ScreenItemTim.this.left = ScreenItemTim.this.cmdContinue;
                        return;
                    } else if (ScreenItemTim.this.type == 5) {
                        ScreenItemTim.this.left = ScreenItemTim.this.cmdXay;
                        return;
                    } else {
                        if (ScreenItemTim.this.type == 6) {
                            ScreenItemTim.this.left = ScreenItemTim.this.cmdDapItemTim;
                            return;
                        }
                        return;
                    }
                }
                ScreenItemTim.this.resetData();
                ScreenItemTim.this.last.switchToMe();
            }
        });
        this.cmdView = new Command(Canvas.isSmartPhone ? "" : "", new AnonymousClass5());
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

    class AnonymousClass5 implements IAction {
        AnonymousClass5() {
        }

        @Override
        public void perform() {
            Vector menu = new Vector();
            menu.addElement(new Command("Bỏ vào", new AnonymousClass6(this)));
            if (!Canvas.isSmartPhone) {
                menu.addElement(new Command("Thông tin", new IAction() {
                    @Override
                    public void perform() {
                        if (Canvas.isSmartPhone) {
                            ScreenItemTim.this.timeDelay = (byte) 5;
                        }
                        if (ScreenItemTim.listItem.size() > 0 && ScreenItemTim.this.currentRow == 1 && ScreenItemTim.this.type != 6) {
                            ScreenItemTim.this.doView();
                        } else if (ScreenItemTim.listItem.size() > 0 && ScreenItemTim.this.type == 6) {
                            ScreenItemTim.this.doView();
                        }
                    }
                }));
            }
            Canvas.menu.startAt(menu, 3);
            Canvas.currentDialog = null;
        }
    }

    class AnonymousClass6 implements IAction {
        final AnonymousClass5 this$1;

        AnonymousClass6(AnonymousClass5 anonymousClass5) {
            this.this$1 = anonymousClass5;
        }

        @Override
        public void perform() {
            if (ScreenItemTim.this.type != 5 || !ScreenItemTim.this.isXayBot) {
                if (ScreenItemTim.this.type != 6 || !ScreenItemTim.this.isDap) {
                    if (ScreenItemTim.this.type == 6 && ScreenItemTim.listItem.size() > 0) {
                        if (ScreenItemTim.this.currentCell == 1) {
                            ScreenItemTim.this.selecItemNL1 = (ItemInInventory) ScreenItemTim.listItem.elementAt(ScreenItemTim.this.selectItemInventory);
                            if (ScreenItemTim.this.checKselecitem(ScreenItemTim.this.selecItemNL1)) {
                                Canvas.startOKDlg("Item Da duoc chon");
                                ScreenItemTim.this.selecItemNL1 = null;
                            }
                        }
                        if (ScreenItemTim.this.currentCell == 2 && ScreenItemTim.selecItemNL2 != null) {
                            ScreenItemTim.this.CheckPaintItem[1] = true;
                        }
                        if (ScreenItemTim.this.currentCell == 3) {
                            ScreenItemTim.this.selecItemNL3 = (ItemInInventory) ScreenItemTim.listItem.elementAt(ScreenItemTim.this.selectItemInventory);
                            if (ScreenItemTim.this.checKselecitem(ScreenItemTim.this.selecItemNL3)) {
                                Canvas.startOKDlg("Item Da duoc chon");
                                ScreenItemTim.this.selecItemNL3 = null;
                            }
                        }
                        if (ScreenItemTim.this.currentCell == 4) {
                            ScreenItemTim.this.selecItemNL4 = (ItemInInventory) ScreenItemTim.listItem.elementAt(ScreenItemTim.this.selectItemInventory);
                            if (ScreenItemTim.this.checKselecitem(ScreenItemTim.this.selecItemNL4)) {
                                Canvas.startOKDlg("Item Da duoc chon");
                                ScreenItemTim.this.selecItemNL4 = null;
                            }
                        }
                        if (ScreenItemTim.this.currentCell == 5) {
                            ScreenItemTim.itemSelect = (ItemInInventory) ScreenItemTim.listItem.elementAt(ScreenItemTim.this.selectItemInventory);
                            if (ScreenItemTim.this.checKselecitem(ScreenItemTim.itemSelect)) {
                                Canvas.startOKDlg("Item Da duoc chon");
                                ScreenItemTim.itemSelect = null;
                            }
                        }
                        if (ScreenItemTim.this.currentCell == 6) {
                            ScreenItemTim.this.selecItemNL6 = (ItemInInventory) ScreenItemTim.listItem.elementAt(ScreenItemTim.this.selectItemInventory);
                            if (ScreenItemTim.this.checKselecitem(ScreenItemTim.this.selecItemNL6)) {
                                Canvas.startOKDlg("Item Da duoc chon");
                                ScreenItemTim.this.selecItemNL6 = null;
                            }
                        }
                    }
                    if (ScreenItemTim.this.type == 5) {
                        if (ScreenItemTim.this.createBot) {
                            ScreenItemTim.this.createBot = false;
                        }
                        if (ScreenItemTim.this.hclip <= 0) {
                            ScreenItemTim.this.hclip = ScreenItemTim.this.size;
                        }
                        if (ScreenItemTim.this.hclip1 > 0) {
                            ScreenItemTim.this.hclip1 = 0;
                        }
                    }
                    if (Canvas.isSmartPhone) {
                        ScreenItemTim.this.timeDelay = (byte) 5;
                    }
                    if (ScreenItemTim.this.currentRow == 1 && ScreenItemTim.this.type != 6 && ScreenItemTim.listItem.size() > 0 && ScreenItemTim.this.selectItemInventory != -1 && ScreenItemTim.this.selectItemInventory <= ScreenItemTim.listItem.size() - 1 && !ScreenItemTim.this.isDap) {
                        if (ScreenItemTim.this.type != 1 || ScreenItemTim.isFinishImbue) {
                            ScreenItemTim.itemSelect = (ItemInInventory) ScreenItemTim.listItem.elementAt(ScreenItemTim.this.selectItemInventory);
                            return;
                        } else {
                            Canvas.startOKDlg("Không thể chọn khi đang trong quá trình luyện tự động.", new IAction() {
                                @Override
                                public void perform() {
                                    ScreenItemTim.this.right.action.perform();
                                    Canvas.endDlg();
                                }
                            });
                            return;
                        }
                    }
                    return;
                }
                Canvas.startOKDlg("Không thể chọn trong quá trình luyện");
                return;
            }
            Canvas.startOKDlg("Không thể chọn trong quá trình luyện");
        }
    }

    private void paintListItem(Graphics g) {
        Res.paintDlgFull(g, xDr, yDr, wCell, hCell, 25, this.tile, false, this.h0 - 28);
        Canvas.resetTrans(g);
        if (this.currentCell != 2) {
            this.mScrollItemInventory.setStyle(listItem.size(), this.wTab, xDr, yDr + 28 + this.h0, wCell - 2, 27, false, 0);
            this.mScrollItemInventory.setClip(g, xDr + 2, yDr + 23 + this.h0, wCell - 4, 31);
            for (int i = 0; i < listItem.size(); i++) {
                if (this.selectItemInventory == i) {
                    g.setColor(0x3D7DCA);
                    g.fillRect(xDr + (this.wTab * i), yDr + 23 + this.h0, this.wTab, 28);
                }
                ItemInInventory item = (ItemInInventory) listItem.elementAt(i);
                item.paint(g, xDr + (this.wTab * i) + (this.wTab / 2), yDr + 36 + this.h0);
                g.setColor(0x6FA6EA);
                g.fillRect(xDr + (this.wTab * i), yDr + 22 + this.h0, 1, 29);
            }
            g.setColor(0x6FA6EA);
            g.fillRect(xDr + (this.wTab * listItem.size()), yDr + 22 + this.h0, 1, 29);
        }
        if (selecItemNL2 != null && this.currentCell == 2) {
            g.setColor(0x3D7DCA);
            g.fillRect(xDr + 3, yDr + 23 + this.h0, this.wTab - 3, 28);
            Res.paintGem(g, Res.getGemByID(selecItemNL2.id).idImage, xDr + (this.wTab / 2), yDr + 36 + this.h0);
            Font.smallFontColor[3].drawString(g, new StringBuffer(String.valueOf(selecItemNL2.number)).toString(), xDr + (this.wTab / 2) + (this.size / 2), ((yDr + 50) + this.h0) - Font.smallFontColor[3].getHeight(), 1);
            g.setColor(0x6FA6EA);
            g.fillRect(xDr + this.wTab, yDr + 22 + this.h0, 1, 29);
        }
        Canvas.resetTrans(g);
        g.setColor(15338765);
        g.fillRect(this.xItem[this.currentCell] - 1, this.yItem[this.currentCell] - 11, this.size + 2, this.size + 2);
    }

    @Override
    public void startXayBot(int idbot, int type, int sl) {
        Canvas.endDlg();
        this.id_bot = (short) idbot;
        this.typeResult = (byte) type;
        this.soluong = (byte) sl;
        addlazer();
        this.isXayBot = true;
    }

    private void paintResult(Graphics g) {
        g.setClip(this.xItem[4] + 4, (this.yItem[4] - 15) + this.hclip + 3, this.size, this.size);
        if (this.typeResult == 0) {
            Res.paintPotion(g, MainChar.listPotion[this.id_bot].index, this.xItem[4] + 4, this.yItem[4] - 6, 0);
        } else {
            GameData.paintIcon(g, (short) (this.id_bot + 6500), this.xItem[4] + 12, this.yItem[4] + 2, 3);
            Font.smallFontColor[3].drawString(g, new StringBuffer(String.valueOf((int) this.soluong)).toString(), this.xItem[4] + 18, this.yItem[4] + 2, 1);
        }
    }

    private void addEffectBot() {
        int xc = Arrow.ARROWSIZE[0][10] / 2;
        for (int i = 0; i < 2; i++) {
            Effect_XayBot e = new Effect_XayBot((this.xItem[2] + xc) - 4, (this.yto + (this.size / 2)) - 10, 2 * i, (this.yItem[4] + (this.size / 2)) - 10, 0);
            this.mEffect.addElement(e);
        }
    }

    private void updatelazer() {
        int xc = Arrow.ARROWSIZE[0][10] / 2;
        for (int i = 0; i < this.lazer.size(); i++) {
            WeaponsLazer w = (WeaponsLazer) this.lazer.elementAt(i);
            if (w != null && this.type == 5) {
                if (this.hclip > 4) {
                    addEffectBot();
                }
                if (Canvas.gameTick % 2 == 0) {
                    Effect e = new Effect((this.xItem[2] + xc) - 4, (this.yto + (this.size / 2)) - 10, 54);
                    this.mEffect.addElement(e);
                }
            }
            w.update2();
            if (Canvas.gameTick % 2 == 0) {
                Effect e2 = new Effect(w.xlim, w.ylim, 54);
                this.mEffect.addElement(e2);
            }
            if (w.wantDestroy) {
                this.lazer.removeElement(w);
                this.isdestroyitem = true;
                this.isShot = true;
            }
        }
    }

    private void paintlazer(Graphics g) {
        for (int i = 0; i < this.lazer.size(); i++) {
            WeaponsLazer w = (WeaponsLazer) this.lazer.elementAt(i);
            w.paint(g);
        }
    }

    public void addlazer() {
        int xc = Arrow.ARROWSIZE[0][10] / 2;
        this.center = this.cmdWaiting;
        this.cmdXay.caption = "";
        Effect e = new Effect((this.xItem[2] + xc) - 4, (this.yto + (this.size / 2)) - 10, 54);
        this.mEffect.addElement(e);
        for (int i = 0; i < this.xItem.length; i++) {
            if (i % 4 != 0) {
                WeaponsLazer w = new WeaponsLazer((this.xItem[i] + xc) - 4, (this.yItem[i] + xc) - 10, (this.xItem[2] + xc) - 4, (this.yto + (this.size / 2)) - 10, 40, 2);
                this.lazer.addElement(w);
            }
        }
    }

    private void updateEffect() {
        for (int i = 0; i < this.mEffect.size(); i++) {
            Effect e = (Effect) this.mEffect.elementAt(i);
            if (e != null) {
                e.update();
            }
            if (e.wantDetroy && e.type == -1) {
                this.createBot = true;
                this.mEffect.removeElement(e);
            }
            if (e.wantDetroy) {
                this.mEffect.removeElement(e);
            }
        }
    }

    private void paintBot(Graphics g) {
        Canvas.resetTrans(g);
        for (int i = 0; i < this.mEffect.size(); i++) {
            Effect e = (Effect) this.mEffect.elementAt(i);
            e.paint(g);
        }
    }

    protected void doView() {
        if (this.type == 6) {
            if (Char.inventory.size() == 0 || this.selectItemInventory < 0 || this.selectItemInventory > listItem.size() - 1) {
                return;
            }
            if (this.currentCell == 2) {
                if (selecItemNL2 == null) {
                    return;
                }
                GemTemp g2 = Res.getGemByID(selecItemNL2.id);
                String itemInfo2 = ItemInInventory.setTemp(g2.name, "0");
                this.arrShowText = Font.normalFont[0].splitFontBStrInLine(new StringBuffer(String.valueOf(itemInfo2)).append(ItemInInventory.setTemp(g2.decript, "0")).toString(), this.wSh - 10);
            }
            if (this.currentCell != 2) {
                ItemInInventory item = (ItemInInventory) listItem.elementAt(this.selectItemInventory);
                if (!item.isEnoughData) {
                    item.isEnoughData = true;
                    GameService.gI().requestItemInfo(item.itemID, Canvas.gameScr.mainChar.ID);
                }
                this.arrShowText = Font.normalFont[0].splitFontBStrInLine(item.getDescription(false), this.wSh - 10);
            }
        }
        if (this.type == 5) {
            if (Char.inventory.size() == 0 || this.selectItemInventory < 0 || this.selectItemInventory > listItem.size() - 1) {
                return;
            }
            ItemInInventory item1 = (ItemInInventory) listItem.elementAt(this.selectItemInventory);
            if (!item1.isEnoughData) {
                item1.isEnoughData = true;
                GameService.gI().requestItemInfo(item1.itemID, Canvas.gameScr.mainChar.ID);
            }
            this.arrShowText = Font.normalFont[0].splitFontBStrInLine(item1.getDescription(false), this.wSh - 10);
        }
        if (!Canvas.isSmartPhone && listItem.size() > 0) {
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
        if (!Canvas.isSmartPhone) {
            this.isShowText = true;
            this.center = this.cmdErr;
            this.left = this.cmdErr;
        }
    }

    public void paintCell(Graphics g) {
        g.setColor(0x003A70);
        int h0 = Canvas.isSmartPhone ? -27 : 10;
        g.fillRect(this.xSh + 2, this.ySh + h0, this.wSh, this.hSh - 8);
        g.setColor(0xFFCB05);
        g.drawRect(this.xSh + 2, this.ySh + h0, this.wSh, this.hSh - 8);
    }

    private void addNguyenlieu() {
        for (int i = 0; i < vnguyenLieu.size(); i++) {
            GemTemplate gem = (GemTemplate) vnguyenLieu.elementAt(i);
        }
    }

    public void paintShowText(Graphics g) {
        Canvas.resetTrans(g);
        if (wCell >= 300) {
            paintCell(g);
            if (!this.isShowText && !Canvas.isSmartPhone) {
                return;
            }
        } else if (!this.isShowText && !Canvas.isSmartPhone) {
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

    private void paintScreenXayBot(Graphics g) {
        Res.paintDlgFull(g, xDr, yDr, wCell, hCell, 25, this.tile, false, this.h0 - 28);
        Canvas.resetTrans(g);
        this.mScrollItemInventory.setStyle(listItem.size(), this.wTab, xDr, yDr + 28 + this.h0, wCell - 2, 27, false, 0);
        this.mScrollItemInventory.setClip(g, xDr + 2, yDr + 23 + this.h0, wCell - 4, 31);
        for (int i = 0; i < listItem.size(); i++) {
            if (this.selectItemInventory == i) {
                g.setColor(0x3D7DCA);
                g.fillRect(xDr + (this.wTab * i), yDr + 23 + this.h0, this.wTab, 28);
            }
            ItemInInventory item = (ItemInInventory) listItem.elementAt(i);
            item.paint(g, xDr + (this.wTab * i) + (this.wTab / 2), yDr + 36 + this.h0);
            g.setColor(0x6FA6EA);
            g.fillRect(xDr + (this.wTab * i), yDr + 22 + this.h0, 1, 29);
            if (itemSelect != null && itemSelect.itemID == item.itemID) {
                g.setColor(0xFFCB05);
                g.drawRect(xDr + (this.wTab * i) + 1, yDr + 23 + this.h0, this.wTab - 2, 28);
                g.drawRect(xDr + (this.wTab * i) + 2, yDr + 24 + this.h0, this.wTab - 4, 26);
            }
        }
        g.setColor(0x6FA6EA);
        g.fillRect(xDr + (this.wTab * listItem.size()), yDr + 22 + this.h0, 1, 29);
        Canvas.resetTrans(g);
    }

    @Override
    public void paint(Graphics g) {
        if (this.last != null) {
            this.last.paint(g);
        }
        if (this.type == 5) {
            paintScreenXayBot(g);
        } else if (this.type == 6) {
            paintListItem(g);
        }
        Canvas.resetTrans(g);
        paintItemEp(g);
        paintlazer(g);
        paintBot(g);
        if (this.type == 5) {
            paintlazer(g);
            paintResult(g);
        }
        for (int i = 0; i < this.mShot.size(); i++) {
            Effect e = (Effect) this.mShot.elementAt(i);
            e.paint(g);
        }
        paintShowText(g);
        super.paint(g);
    }

    public boolean CheckHaveItemcell() {
        for (int i = 0; i < this.CheckHaveItemCell.length; i++) {
            if (!this.CheckHaveItemCell[i]) {
                return false;
            }
        }
        return true;
    }

    public void paintItemEp(Graphics g) {
        if (this.type == 5) {
            for (int i = 0; i < this.xItem.length; i++) {
                if (i % 4 == 0) {
                    g.setColor(16777215);
                    g.fillRect(this.xItem[i], this.yItem[i] - 10, this.size, this.size);
                    g.setColor(0x3D7DCA);
                    g.fillRect(this.xItem[i] + 1, this.yItem[i] - 9, this.size - 2, this.size - 2);
                } else if (Res.getImgArrow(10) != null) {
                    g.drawRegion(Res.getImgArrow(10), 0, this.frame * Arrow.ARROWSIZE[1][10], Arrow.ARROWSIZE[0][10], Arrow.ARROWSIZE[1][10], 0, this.xItem[i] - 3, this.yItem[i] - 10, 0);
                }
            }
        } else if (this.type == 6) {
            for (int i2 = 0; i2 < this.xItem.length; i2++) {
                g.setColor(16777215);
                g.fillRect(this.xItem[i2], this.yItem[i2] - 10, this.size, this.size);
                g.setColor(0x3D7DCA);
                g.fillRect(this.xItem[i2] + 1, this.yItem[i2] - 9, this.size - 2, this.size - 2);
            }
        }
        if (this.type != 1) {
            if (itemSelect != null && this.type != 5 && this.type != 6) {
                itemSelect.paint(g, this.xItem[0] + 10, this.yItem[0]);
            }
            if (this.type == 6) {
                if (itemSelect != null) {
                    itemSelect.paint(g, this.xItem[5] + (this.size / 2), this.yItem[5] + 1);
                    this.CheckHaveItemCell[5] = true;
                }
                if (this.selecItemNL1 != null) {
                    this.selecItemNL1.paint(g, this.xItem[1] + (this.size / 2), this.yItem[1] + 1);
                    this.CheckHaveItemCell[0] = true;
                }
                if (this.CheckPaintItem[1]) {
                    Res.paintGem(g, Res.getGemByID(selecItemNL2.id).idImage, this.xItem[2] + (this.size / 2), this.yItem[2]);
                    this.CheckHaveItemCell[1] = true;
                }
                if (this.selecItemNL3 != null) {
                    this.selecItemNL3.paint(g, this.xItem[3] + (this.size / 2), this.yItem[3] + 1);
                    this.CheckHaveItemCell[2] = true;
                }
                if (this.selecItemNL4 != null) {
                    this.selecItemNL4.paint(g, this.xItem[4] + (this.size / 2), this.yItem[4] + 1);
                    this.CheckHaveItemCell[3] = true;
                }
                if (this.selecItemNL6 != null) {
                    this.selecItemNL6.paint(g, this.xItem[6] + (this.size / 2), this.yItem[6] + 1);
                    this.CheckHaveItemCell[4] = true;
                }
            }
            if (itemSelect != null && this.type == 5) {
                Canvas.resetTrans(g);
                g.setClip(this.xItem[0], ((this.yItem[0] - (this.size / 4)) + this.hclip1) - 5, this.size, this.size);
                itemSelect.paint(g, this.xItem[0] + 11, this.yItem[0] + 1);
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
        paintlazer(g);
    }

    private void updateResult() {
        if (this.createBot && Canvas.gameTick % 2 == 0 && this.hclip > 0) {
            this.hclip--;
            this.hclip1++;
        }
    }

    @Override
    public void update() {
        updateResult();
        updatelazer();
        updateEffect();
        if (this.type == 6 && this.isDap) {
            this.center = this.cmdWaiting;
            this.cmdDapItemTim.caption = "";
            if (this.timeDlay >= 0) {
                this.timeDlay--;
            }
            if (this.timeDlay < 0 && this.lazer.size() <= 0) {
                this.timeDlay = 15;
                resetCheck();
                this.isDap = false;
            }
        }
        if (Canvas.isSmartPhone && listItem.size() > 0) {
            doView();
        }
        if (this.dem >= 0) {
            this.dem--;
        }
        if (this.dem < 0 && this.isShot && Canvas.gameTick % 15 == 0 && this.tick < 3 && this.type == 6) {
            for (int i = 1; i < this.xItem.length; i++) {
                this.mShot.addElement(new EffectDapDoTim(this.xItem[i] + (this.size / 2), this.yItem[i], this.xItem[0] + (this.size / 2), this.yItem[0]));
            }
            this.tick++;
        }
        for (int i2 = 0; i2 < this.mShot.size(); i2++) {
            Effect e = (Effect) this.mShot.elementAt(i2);
            e.update();
            if (e.wantDetroy) {
                for (int j = 0; j < 10; j++) {
                    Effect_XayBot obj = new Effect_XayBot(this.xItem[0] + (this.size / 2), this.yItem[0], 0, this.yItem[0] + 80, -1);
                    this.mEffect.addElement(obj);
                }
                Effect eff = new Effect(this.xItem[0] + (this.size / 2), this.yItem[0], 50);
                this.mEffect.addElement(eff);
                this.mDestroy.addElement(e);
            }
        }
        for (int i3 = 0; i3 < this.mDestroy.size(); i3++) {
            this.mShot.removeElement(this.mDestroy.elementAt(i3));
        }
        this.mDestroy.removeAllElements();
        if (this.type == 5 || this.type == 6) {
            this.currentRow = (byte) 1;
        }
        this.frame = (byte) (this.frame + 1);
        if (this.frame >= 3) {
            this.frame = (byte) 0;
        }
        if (this.isdestroyitem && this.mEffect.size() <= 0 && this.type == 5) {
            if (this.timeDlay >= 0) {
                this.timeDlay--;
            }
            if (this.timeDlay < 0) {
                itemSelect = null;
                this.isdestroyitem = false;
                this.isXayBot = false;
                this.timeDlay = 15;
                this.cmdXay.caption = "Nghiền";
                this.center = this.cmdView;
                resetSelect();
            }
        }
        if (this.last != null) {
            this.last.update();
        }
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
        if (Canvas.isPointerClick && this.type == 6) {
            Canvas.isPointerClick = false;
            for (int i4 = 1; i4 < this.xItem.length; i4++) {
                if (Canvas.px >= this.xItem[i4] && Canvas.px <= this.xItem[i4] + this.size && Canvas.py >= this.yItem[i4] - 10 && Canvas.py <= (this.yItem[i4] - 10) + this.size) {
                    this.currentCell = (byte) i4;
                    this.selectItemInventory = 0;
                    this.mScrollItemInventory.moveTo(this.wTab);
                }
            }
        }
        super.update();
    }

    public boolean checKselecitem(ItemInInventory item) {
        switch (this.currentCell) {
            case 1:
                if (itemSelect == null || itemSelect.itemID != item.itemID) {
                    if (this.selecItemNL3 == null || this.selecItemNL3.itemID != item.itemID) {
                        if (this.selecItemNL4 == null || this.selecItemNL4.itemID != item.itemID) {
                            if (this.selecItemNL6 != null && this.selecItemNL6.itemID == item.itemID) {
                                return true;
                            }
                            return false;
                        }
                        return true;
                    }
                    return true;
                }
                return true;
            case 2:
            default:
                return false;
            case 3:
                if (itemSelect == null || itemSelect.itemID != item.itemID) {
                    if (this.selecItemNL1 == null || this.selecItemNL1.itemID != item.itemID) {
                        if (this.selecItemNL4 == null || this.selecItemNL4.itemID != item.itemID) {
                            if (this.selecItemNL6 != null && this.selecItemNL6.itemID == item.itemID) {
                                return true;
                            }
                            return false;
                        }
                        return true;
                    }
                    return true;
                }
                return true;
            case 4:
                if (itemSelect == null || itemSelect.itemID != item.itemID) {
                    if (this.selecItemNL3 == null || this.selecItemNL3.itemID != item.itemID) {
                        if (this.selecItemNL1 == null || this.selecItemNL1.itemID != item.itemID) {
                            if (this.selecItemNL6 != null && this.selecItemNL6.itemID == item.itemID) {
                                return true;
                            }
                            return false;
                        }
                        return true;
                    }
                    return true;
                }
                return true;
            case 5:
                if (this.selecItemNL1 == null || this.selecItemNL1.itemID != item.itemID) {
                    if (this.selecItemNL3 == null || this.selecItemNL3.itemID != item.itemID) {
                        if (this.selecItemNL4 == null || this.selecItemNL4.itemID != item.itemID) {
                            if (this.selecItemNL6 != null && this.selecItemNL6.itemID == item.itemID) {
                                return true;
                            }
                            return false;
                        }
                        return true;
                    }
                    return true;
                }
                return true;
            case 6:
                if (itemSelect == null || itemSelect.itemID != item.itemID) {
                    if (this.selecItemNL3 == null || this.selecItemNL3.itemID != item.itemID) {
                        if (this.selecItemNL4 == null || this.selecItemNL4.itemID != item.itemID) {
                            if (this.selecItemNL1 != null && this.selecItemNL1.itemID == item.itemID) {
                                return true;
                            }
                            return false;
                        }
                        return true;
                    }
                    return true;
                }
                return true;
        }
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

    public void addlazeEpItemTim() {
        WeaponsLazer w = new WeaponsLazer(this.xItem[6] + (this.size / 2), this.yItem[6], this.xItem[1] + (this.size / 2), this.yItem[1], 100, 3, 3);
        this.lazer.addElement(w);
        for (int i = 1; i < this.xItem.length - 1; i++) {
            WeaponsLazer w2 = new WeaponsLazer(this.xItem[i] + (this.size / 2), this.yItem[i], this.xItem[i + 1] + (this.size / 2), this.yItem[i + 1], 100, 3, 6 + (i * 2));
            this.lazer.addElement(w2);
            this.dem = 6 + (i * 2);
        }
    }

    @Override
    public void updateKey() {
        if (this.isShowText) {
            if (Canvas.keyPressed[2] && this.type != 5) {
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
            if (Canvas.keyPressed[2] && this.type != 5) {
                Canvas.keyPressed[2] = false;
                this.currentCell = (byte) (this.currentCell - 1);
                if (this.selecItemNL1 != null) {
                    this.LisItemSelect.addElement(this.selecItemNL1);
                }
                if (this.selecItemNL3 != null) {
                    this.LisItemSelect.addElement(this.selecItemNL3);
                }
                if (this.selecItemNL4 != null) {
                    this.LisItemSelect.addElement(this.selecItemNL4);
                }
                if (itemSelect != null) {
                    this.LisItemSelect.addElement(itemSelect);
                }
                if (this.selecItemNL6 != null) {
                    this.LisItemSelect.addElement(this.selecItemNL6);
                }
                this.DropItem = false;
                if (this.currentCell < 1) {
                    this.currentCell = (byte) 6;
                }
                if (this.type == 6) {
                    this.selectItemInventory = 0;
                    this.mScrollItemInventory.moveTo(this.wTab);
                }
            } else if (Canvas.keyPressed[8]) {
                Canvas.keyPressed[8] = false;
                this.DropItem = false;
                if (this.selecItemNL1 != null) {
                    this.LisItemSelect.addElement(this.selecItemNL1);
                }
                if (this.selecItemNL3 != null) {
                    this.LisItemSelect.addElement(this.selecItemNL3);
                }
                if (this.selecItemNL4 != null) {
                    this.LisItemSelect.addElement(this.selecItemNL4);
                }
                if (itemSelect != null) {
                    this.LisItemSelect.addElement(itemSelect);
                }
                if (this.selecItemNL6 != null) {
                    this.LisItemSelect.addElement(this.selecItemNL6);
                }
                this.currentCell = (byte) (this.currentCell + 1);
                if (this.currentCell > 6) {
                    this.currentCell = (byte) 1;
                }
                if (this.type == 6) {
                    this.selectItemInventory = 0;
                    this.mScrollItemInventory.moveTo(this.wTab);
                }
            }
            if (Canvas.keyPressed[4]) {
                Canvas.keyPressed[4] = false;
                this.selectItemInventory--;
                if (this.selectItemInventory < 0) {
                    this.selectItemInventory = listItem.size() - 1;
                }
                this.mScrollItemInventory.moveTo(this.selectItemInventory * this.wTab);
            } else if (Canvas.keyPressed[6]) {
                Canvas.keyPressed[6] = false;
                this.selectItemInventory++;
                if (this.selectItemInventory > listItem.size() - 1) {
                    this.selectItemInventory = 0;
                }
                this.mScrollItemInventory.moveTo(this.selectItemInventory * this.wTab);
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
                if (this.type == 6) {
                    selecItemNL2 = gem;
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
            if (this.type == 0) {
                listItem = ItemInInventory.getAllItem(COLOR_GREEN);
            } else if (this.type == 2) {
                listItem = ItemInInventory.getAllItemAnimal(COLOR_GREEN, 1);
            } else if (this.type == 3) {
                listItem = ItemInInventory.getAllItemCharExcepColor(COLOR_WHITE);
            } else {
                listItem = ItemInInventory.getAllItemCharDrop();
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

    private void resetCheck() {
        selectGem = null;
        this.cmdDapItemTim.caption = "Đập";
        this.center = this.cmdView;
        this.isShot = false;
        this.tick = 0;
        this.selecItemNL1 = null;
        selecItemNL2 = null;
        this.selecItemNL3 = null;
        this.selecItemNL4 = null;
        this.selecItemNL6 = null;
        this.LisItemSelect.removeAllElements();
        for (int i = 0; i < this.CheckHaveItemCell.length; i++) {
            this.CheckHaveItemCell[i] = false;
            this.CheckPaintItem[i] = false;
        }
        itemSelect = null;
        getListGem(true, false);
    }

    public void resetSelect() {
        if (this.type == 5) {
            this.center = this.cmdView;
        }
        this.isShot = false;
        this.tick = 0;
        this.isDap = false;
        resetCheck();
        this.currentCell = (byte) 5;
        this.isXayBot = false;
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
